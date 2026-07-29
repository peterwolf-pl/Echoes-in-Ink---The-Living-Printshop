package pl.peterwolf.echoesinink.gametest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.lwjgl.glfw.GLFW;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.block.ModBlocks;
import pl.peterwolf.echoesinink.block.PressPhase;
import pl.peterwolf.echoesinink.block.PrintingPressBlock;
import pl.peterwolf.echoesinink.block.entity.PrintingPressBlockEntity;
import pl.peterwolf.echoesinink.item.ModItems;

/**
 * Durable end-to-end client test for the printing press.
 *
 * <p>The test runs against an integrated singleplayer server, so it covers the
 * server-authoritative press state machine, block-entity synchronization and
 * the real client block-entity renderer in one run.</p>
 */
public final class PrintingPressClientGameTest implements FabricClientGameTest {
	private static final BlockPos PRESS_POS = new BlockPos(0, 4, 0);
	private static final String SCREENSHOT_NAME = "printing_press_output_ready";

	@Override
	public void runTest(ClientGameTestContext context) {
		context.getInput().resizeWindow(1920, 1080);

		try (TestSingleplayerContext singleplayer = context.worldBuilder()
			.setUseConsistentSettings(true)
			.create()) {
			TestServerContext serverContext = singleplayer.getServer();
			singleplayer.getClientLevel().waitForChunksRender();

			serverContext.runOnServer(server -> {
				ServerPlayer player = requirePlayer(server.getPlayerList().getPlayers());
				var level = server.overworld();

				buildScene(level);
				var blockEntity = level.getBlockEntity(PRESS_POS);
				if (!(blockEntity instanceof PrintingPressBlockEntity press)) {
					throw new AssertionError("Printing press block entity was not created at " + PRESS_POS);
				}

				press.forceAssemble();
				assertPhase(press, PressPhase.IDLE);
				insert(press, player, new ItemStack(ModItems.WOODEN_PRINTING_MATRIX), "wooden matrix");
				insert(press, player, new ItemStack(ModItems.INK_BALL), "ink ball");
				insert(press, player, new ItemStack(ModItems.BLANK_ARCHIVE_PAGE), "blank page");

				press.interactEmptyHand(player);
				assertPhase(press, PressPhase.CARRIAGE_IN);
				press.interactEmptyHand(player);
				assertPhase(press, PressPhase.PRESSING);
			});

			// 60 recipe ticks + 20 resetting ticks, with a buffer for sync/render.
			context.waitTicks(100);

			serverContext.runOnServer(server -> {
				ServerPlayer player = requirePlayer(server.getPlayerList().getPlayers());
				var blockEntity = server.overworld().getBlockEntity(PRESS_POS);
				if (!(blockEntity instanceof PrintingPressBlockEntity press)) {
					throw new AssertionError("Printing press disappeared during the cycle");
				}

				assertPhase(press, PressPhase.IMPRESSION_DONE);
				press.interactEmptyHand(player);
				assertPhase(press, PressPhase.OUTPUT_READY);

				if (press.getItem(PrintingPressBlockEntity.SLOT_OUTPUT).getItem()
					!= ModItems.PRINTERS_INSTRUCTION_SHEET) {
					throw new AssertionError("Expected printer's instruction sheet in output slot");
				}
				if (!press.getItem(PrintingPressBlockEntity.SLOT_INK).isEmpty()) {
					throw new AssertionError("Ink was not consumed");
				}
				if (!press.getItem(PrintingPressBlockEntity.SLOT_PAPER).isEmpty()) {
					throw new AssertionError("Blank page was not consumed");
				}
				if (press.getItem(PrintingPressBlockEntity.SLOT_MATRIX).getItem()
					!= ModItems.WOODEN_PRINTING_MATRIX) {
					throw new AssertionError("Reusable wooden matrix was not retained");
				}
			});

			context.waitFor(client ->
				client.level != null
					&& client.level.getBlockEntity(PRESS_POS) instanceof PrintingPressBlockEntity press
					&& press.phase() == PressPhase.OUTPUT_READY
					&& press.getItem(PrintingPressBlockEntity.SLOT_OUTPUT).is(ModItems.PRINTERS_INSTRUCTION_SHEET),
				200
			);
			context.runOnClient(client -> {
				if (client.level == null
					|| !(client.level.getBlockEntity(PRESS_POS) instanceof PrintingPressBlockEntity press)) {
					throw new AssertionError("Client printing press was unavailable after synchronization");
				}
				assertPhase(press, PressPhase.OUTPUT_READY);
				if (!press.getItem(PrintingPressBlockEntity.SLOT_OUTPUT)
					.is(ModItems.PRINTERS_INSTRUCTION_SHEET)) {
					throw new AssertionError("Client did not synchronize the printed instruction sheet");
				}

				logFixedModelBounds(client, "printers_instruction_sheet",
					new ItemStack(ModItems.PRINTERS_INSTRUCTION_SHEET));
				logFixedModelBounds(client, "press_carriage",
					new ItemStack(ModItems.PRESS_CARRIAGE));
			});

			serverContext.runCommand("time set noon");
			serverContext.runCommand("weather clear");
			serverContext.runCommand("gamemode spectator @a");
			serverContext.runCommand("tp @a 1.6 5.0 -1.8 42 32");
			context.waitTicks(10);
			singleplayer.getClientLevel().waitForChunksRender();
			context.getInput().pressKey(GLFW.GLFW_KEY_F1);
			context.waitTicks(2);

			Path screenshotDirectory = screenshotDirectory();
			Path screenshot = context.takeScreenshot(
				TestScreenshotOptions.of(SCREENSHOT_NAME)
					.disableCounterPrefix()
					.withSize(1920, 1080)
					.withDestinationDir(screenshotDirectory)
			);
			if (!Files.isRegularFile(screenshot)) {
				throw new AssertionError("Client game test did not create screenshot: " + screenshot);
			}
			context.getInput().pressKey(GLFW.GLFW_KEY_F1);
			EchoesInInk.LOGGER.info("Printing press ClientGameTest screenshot: {}", screenshot.toAbsolutePath());
		}
	}

	private static void buildScene(net.minecraft.server.level.ServerLevel level) {
		for (int x = -5; x <= 5; x++) {
			for (int z = -4; z <= 7; z++) {
				level.setBlockAndUpdate(new BlockPos(x, 3, z), Blocks.SMOOTH_STONE.defaultBlockState());
			}
		}
		for (int y = 4; y <= 7; y++) {
			level.setBlockAndUpdate(new BlockPos(-4, y, -3), Blocks.STRIPPED_OAK_LOG.defaultBlockState());
			level.setBlockAndUpdate(new BlockPos(4, y, -3), Blocks.STRIPPED_OAK_LOG.defaultBlockState());
		}
		for (int x = -4; x <= 4; x++) {
			level.setBlockAndUpdate(new BlockPos(x, 7, -3), Blocks.DARK_OAK_PLANKS.defaultBlockState());
		}

		level.setBlockAndUpdate(
			PRESS_POS,
			ModBlocks.PRINTING_PRESS.defaultBlockState()
				.setValue(PrintingPressBlock.FACING, Direction.NORTH)
		);
	}

	private static ServerPlayer requirePlayer(java.util.List<ServerPlayer> players) {
		if (players.isEmpty()) {
			throw new AssertionError("Singleplayer test started without a server player");
		}
		return players.getFirst();
	}

	private static void insert(
		PrintingPressBlockEntity press,
		ServerPlayer player,
		ItemStack stack,
		String label
	) {
		if (!press.tryInsertInput(player, stack)) {
			throw new AssertionError("Printing press rejected " + label);
		}
	}

	private static void assertPhase(PrintingPressBlockEntity press, PressPhase expected) {
		if (press.phase() != expected) {
			throw new AssertionError("Expected press phase " + expected + ", got " + press.phase());
		}
	}

	private static void logFixedModelBounds(Minecraft client, String modelName, ItemStack stack) {
		ItemStackRenderState renderState = new ItemStackRenderState();
		client.getItemModelResolver().updateForTopItem(
			renderState,
			stack,
			ItemDisplayContext.FIXED,
			client.level,
			null,
			42
		);
		if (renderState.isEmpty()) {
			throw new AssertionError("FIXED item model did not resolve for " + modelName);
		}

		AABB bounds = renderState.getModelBoundingBox();
		EchoesInInk.LOGGER.info(
			"FIXED_MODEL_BOUNDS {} min=({}, {}, {}) max=({}, {}, {})",
			modelName,
			bounds.minX,
			bounds.minY,
			bounds.minZ,
			bounds.maxX,
			bounds.maxY,
			bounds.maxZ
		);
	}

	private static Path screenshotDirectory() {
		Path directory = Path.of(System.getProperty("user.dir"))
			.resolve("client-gametest-screenshots")
			.toAbsolutePath()
			.normalize();
		try {
			return Files.createDirectories(directory);
		} catch (IOException exception) {
			throw new AssertionError("Could not create screenshot directory " + directory, exception);
		}
	}
}
