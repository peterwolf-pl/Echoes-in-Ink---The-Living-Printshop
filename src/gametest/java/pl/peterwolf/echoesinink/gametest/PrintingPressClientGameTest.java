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
	private static final String OUTPUT_SCREENSHOT_NAME = "printing_press_output_ready";

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
				insert(press, player, new ItemStack(ModItems.METAL_TYPE_PIECE), "composed metal type forme");
				if (press.matrixInked()) {
					throw new AssertionError("Fresh metal type was already marked as inked");
				}
			});

			serverContext.runCommand("time set noon");
			serverContext.runCommand("weather clear");
			serverContext.runCommand("gamemode spectator @a");
			serverContext.runCommand("tp @a 0.5 4.3 -1.5 0 25");
			context.waitTicks(10);
			singleplayer.getClientLevel().waitForChunksRender();
			context.getInput().pressKey(GLFW.GLFW_KEY_F1);
			context.waitTicks(2);

			serverContext.runOnServer(server -> {
				ServerPlayer player = requirePlayer(server.getPlayerList().getPlayers());
				var blockEntity = server.overworld().getBlockEntity(PRESS_POS);
				if (!(blockEntity instanceof PrintingPressBlockEntity press)) {
					throw new AssertionError("Printing press disappeared before inking");
				}
				// Regression: metal type previously accepted an ink pad, then falsely
				// entered JAMMED because only the ink-ball recipe was registered.
				insert(press, player, new ItemStack(ModItems.INK_PAD), "ink pad");
				assertPhase(press, PressPhase.INKING);
				if (press.matrixInked()) {
					throw new AssertionError("Metal type became fully inked before the animation");
				}
			});
			context.waitFor(client ->
				client.level != null
					&& client.level.getBlockEntity(PRESS_POS) instanceof PrintingPressBlockEntity press
					&& press.phase() == PressPhase.INKING
					&& !press.matrixInked(),
				100
			);
			context.waitTicks(10);
			takeScreenshot(context, "printing_press_metal_type_inking");

			context.waitFor(client ->
				client.level != null
					&& client.level.getBlockEntity(PRESS_POS) instanceof PrintingPressBlockEntity press
					&& press.phase() == PressPhase.IDLE
					&& press.matrixInked(),
				100
			);
			context.waitTicks(2);
			takeScreenshot(context, "printing_press_metal_type_inked");

			serverContext.runOnServer(server -> {
				ServerPlayer player = requirePlayer(server.getPlayerList().getPlayers());
				var blockEntity = server.overworld().getBlockEntity(PRESS_POS);
				if (!(blockEntity instanceof PrintingPressBlockEntity press)) {
					throw new AssertionError("Printing press disappeared after inking");
				}
				assertPhase(press, PressPhase.IDLE);
				if (!press.matrixInked()) {
					throw new AssertionError("Printing surface was not inked before paper loading");
				}
				insert(press, player, new ItemStack(ModItems.BLANK_ARCHIVE_PAGE), "blank page");
				press.interactEmptyHand(player);
				assertPhase(press, PressPhase.CARRIAGE_IN);
				press.interactEmptyHand(player);
				assertPhase(press, PressPhase.PRESSING);
			});

			// Capture near maximum pressure: the raised handle must still clear the
			// 28/16-block upper timber while the screw follows the descending platen.
			serverContext.runCommand("tp @a -0.8 4.3 -1.5 -30 25");
			context.waitTicks(36);
			takeScreenshot(context, "printing_press_handle_clearance_under_pressure");
			// Finish 40 recipe ticks + 20 resetting ticks, with a sync/render buffer.
			context.waitTicks(44);

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
					!= ModItems.PRINTED_WARNING_POSTER) {
					throw new AssertionError("Expected printed warning poster in output slot");
				}
				if (!press.getItem(PrintingPressBlockEntity.SLOT_INK).isEmpty()) {
					throw new AssertionError("Ink was not consumed");
				}
				if (!press.getItem(PrintingPressBlockEntity.SLOT_PAPER).isEmpty()) {
					throw new AssertionError("Blank page was not consumed");
				}
				if (press.getItem(PrintingPressBlockEntity.SLOT_MATRIX).getItem()
					!= ModItems.METAL_TYPE_PIECE) {
					throw new AssertionError("Reusable composed type forme was not retained");
				}
				if (press.matrixInked()) {
					throw new AssertionError("Printing surface remained charged after the impression");
				}
			});

			context.waitFor(client ->
				client.level != null
					&& client.level.getBlockEntity(PRESS_POS) instanceof PrintingPressBlockEntity press
					&& press.phase() == PressPhase.OUTPUT_READY
					&& press.getItem(PrintingPressBlockEntity.SLOT_OUTPUT).is(ModItems.PRINTED_WARNING_POSTER),
				200
			);
			context.runOnClient(client -> {
				if (client.level == null
					|| !(client.level.getBlockEntity(PRESS_POS) instanceof PrintingPressBlockEntity press)) {
					throw new AssertionError("Client printing press was unavailable after synchronization");
				}
				assertPhase(press, PressPhase.OUTPUT_READY);
				if (!press.getItem(PrintingPressBlockEntity.SLOT_OUTPUT)
					.is(ModItems.PRINTED_WARNING_POSTER)) {
					throw new AssertionError("Client did not synchronize the printed warning poster");
				}

				logFixedModelBounds(client, "printed_warning_poster",
					new ItemStack(ModItems.PRINTED_WARNING_POSTER));
				logFixedModelBounds(client, "press_carriage",
					new ItemStack(ModItems.PRESS_CARRIAGE));
				AABB typeBounds = logFixedModelBounds(client, "composed_metal_type_forme",
					new ItemStack(ModItems.METAL_TYPE_PIECE));
				if (typeBounds.maxX - typeBounds.minX < 0.8D
					|| typeBounds.maxY - typeBounds.minY < 0.2D
					|| typeBounds.maxZ - typeBounds.minZ < 0.8D) {
					throw new AssertionError("Metal type is not a full 3D printing form: " + typeBounds);
				}
				AABB handleBounds = logFixedModelBounds(client, "press_handle",
					new ItemStack(ModItems.PRESS_HANDLE));
				if (handleBounds.maxX - handleBounds.minX < 1.99D) {
					throw new AssertionError("Press handle is not two blocks long: " + handleBounds);
				}
				AABB screwBounds = logFixedModelBounds(client, "extended_press_screw",
					new ItemStack(ModItems.PRESS_SCREW));
				if (screwBounds.maxY - screwBounds.minY < 1.0D) {
					throw new AssertionError("Press screw does not extend above the upper frame: " + screwBounds);
				}
			});

			serverContext.runCommand("tp @a 0.5 4.3 -1.5 0 25");
			context.waitTicks(10);
			singleplayer.getClientLevel().waitForChunksRender();

			takeScreenshot(context, OUTPUT_SCREENSHOT_NAME);

			serverContext.runOnServer(server -> {
				ServerPlayer player = requirePlayer(server.getPlayerList().getPlayers());
				var blockEntity = server.overworld().getBlockEntity(PRESS_POS);
				if (!(blockEntity instanceof PrintingPressBlockEntity press)) {
					throw new AssertionError("Printing press disappeared before output collection");
				}
				press.interactEmptyHand(player);
				assertPhase(press, PressPhase.IDLE);
				if (!press.getItem(PrintingPressBlockEntity.SLOT_OUTPUT).isEmpty()) {
					throw new AssertionError("Output remained in the press after collection");
				}
			});
			context.waitFor(client ->
				client.level != null
					&& client.level.getBlockEntity(PRESS_POS) instanceof PrintingPressBlockEntity press
					&& press.phase() == PressPhase.IDLE
					&& press.getItem(PrintingPressBlockEntity.SLOT_OUTPUT).isEmpty(),
				200
			);

			// The reported clipping only appeared after collection and changed while
			// panning, so preserve close screenshots of the empty carriage from three
			// camera angles instead of validating only the paper-covered output state.
			serverContext.runCommand("tp @a -0.8 4.3 -1.5 -30 25");
			context.waitTicks(8);
			singleplayer.getClientLevel().waitForChunksRender();
			takeScreenshot(context, "printing_press_empty_carriage_left");

			serverContext.runCommand("tp @a 0.5 4.3 -1.5 0 25");
			context.waitTicks(8);
			singleplayer.getClientLevel().waitForChunksRender();
			takeScreenshot(context, "printing_press_empty_carriage_center");

			serverContext.runCommand("tp @a 1.8 4.3 -1.5 30 25");
			context.waitTicks(8);
			singleplayer.getClientLevel().waitForChunksRender();
			takeScreenshot(context, "printing_press_empty_carriage_right");

			context.getInput().pressKey(GLFW.GLFW_KEY_F1);
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

	private static AABB logFixedModelBounds(Minecraft client, String modelName, ItemStack stack) {
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
		return bounds;
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

	private static Path takeScreenshot(ClientGameTestContext context, String name) {
		Path screenshot = context.takeScreenshot(
			TestScreenshotOptions.of(name)
				.disableCounterPrefix()
				.withSize(1920, 1080)
				.withDestinationDir(screenshotDirectory())
		);
		if (!Files.isRegularFile(screenshot)) {
			throw new AssertionError("Client game test did not create screenshot: " + screenshot);
		}
		EchoesInInk.LOGGER.info("Printing press ClientGameTest screenshot: {}", screenshot.toAbsolutePath());
		return screenshot;
	}
}
