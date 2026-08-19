package pl.peterwolf.echoesinink.gametest;

import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.block.HangingPosterBlock;
import pl.peterwolf.echoesinink.block.InvestigatableBlock;
import pl.peterwolf.echoesinink.block.InvestigationState;
import pl.peterwolf.echoesinink.block.LaidPaperBlock;
import pl.peterwolf.echoesinink.block.ModBlocks;
import pl.peterwolf.echoesinink.block.entity.InvestigationBlockEntity;
import pl.peterwolf.echoesinink.block.entity.LaidPaperBlockEntity;
import pl.peterwolf.echoesinink.item.ModItems;

/** Exercises the actual held-item route for the lens and timed printer's brush. */
public final class InvestigationToolsClientGameTest implements FabricClientGameTest {
	private static final BlockPos PLAQUE_POS = new BlockPos(0, 4, 0);
	private static final BlockPos MATRIX_POS = new BlockPos(2, 4, 0);
	private static final BlockPos PRINT_POS = new BlockPos(3, 4, 0);
	private static final BlockPos POSTER_POS = new BlockPos(1, 5, 2);

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
			.setUseConsistentSettings(true)
			.create()) {
			TestServerContext serverContext = singleplayer.getServer();
			singleplayer.getClientLevel().waitForChunksRender();

			serverContext.runOnServer(server -> {
				ServerPlayer player = requirePlayer(server.getPlayerList().getPlayers());
				ServerLevel level = server.overworld();
				buildScene(level);

				// Set the global message cooldown, then immediately inspect the plaque.
				// The plaque must still remember the lens inspection.
				assertLensHandled(level, player, MATRIX_POS, Direction.UP, "laid matrix");
				assertLensHandled(level, player, PLAQUE_POS, Direction.NORTH, "faded plaque");
				InvestigationBlockEntity plaque = requirePlaque(level);
				if (!plaque.isLensInspected()) {
					throw new AssertionError("Plaque did not retain lens inspection during message cooldown");
				}

				assertLensHandled(level, player, PRINT_POS, Direction.UP, "laid finished print");
				assertLensHandled(level, player, POSTER_POS, Direction.NORTH, "hanging poster");

				equipBrush(player);
			});

			serverContext.runCommand("gamemode creative @a");
			serverContext.runCommand("tp @a 0.5 4.0 -1.5 0 0");
			context.waitTicks(2);
			context.getInput().lookAt(PLAQUE_POS);
			context.waitTicks(2);
			takeScreenshot(context, "investigation_plaque_lens_inspected");
			context.getInput().holdMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
			context.waitTicks(3);
			serverContext.runOnServer(server -> assertBrushUsing(
				requirePlayer(server.getPlayerList().getPlayers())
			));
			// Deliberately look away: the thin plaque must remain the held-use target.
			context.getInput().lookAt(180.0F, -60.0F);
			context.waitTicks(42);
			context.getInput().releaseMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
			context.waitTicks(2);
			context.getInput().lookAt(PLAQUE_POS);
			context.waitTicks(2);
			takeScreenshot(context, "investigation_plaque_partially_cleaned");
			serverContext.runOnServer(server -> {
				ServerPlayer player = requirePlayer(server.getPlayerList().getPlayers());
				ServerLevel level = server.overworld();
				assertPlaqueState(level, InvestigationState.PARTIALLY_CLEANED);
				if (player.isUsingItem()) {
					throw new AssertionError("Printer's Brush did not finish its first cleaning hold");
				}
				equipBrush(player);
			});

			context.waitTicks(2);
			context.getInput().lookAt(PLAQUE_POS);
			context.getInput().holdMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
			context.waitTicks(3);
			serverContext.runOnServer(server -> assertBrushUsing(
				requirePlayer(server.getPlayerList().getPlayers())
			));
			context.getInput().lookAt(180.0F, -60.0F);
			context.waitTicks(42);
			context.getInput().releaseMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
			context.waitTicks(2);
			context.getInput().lookAt(PLAQUE_POS);
			context.waitTicks(2);
			takeScreenshot(context, "investigation_plaque_fully_cleaned");
			serverContext.runOnServer(server -> {
				ServerLevel level = server.overworld();
				assertPlaqueState(level, InvestigationState.FULLY_INVESTIGATED);
				if (!requirePlaque(level).isLootGenerated()) {
					throw new AssertionError("Fully cleaned plaque did not produce its one-time reward");
				}
				EchoesInInk.LOGGER.info(
					"INVESTIGATION_TOOLS_GAMETEST_OK lens=plaque+matrix+print+poster brush=two_holds"
				);
			});
		}
	}

	private static void buildScene(ServerLevel level) {
		for (int x = -1; x <= 4; x++) {
			for (int z = -2; z <= 3; z++) {
				level.setBlockAndUpdate(new BlockPos(x, 3, z), Blocks.SMOOTH_STONE.defaultBlockState());
			}
		}
		level.setBlockAndUpdate(PLAQUE_POS, ModBlocks.FADED_WORKSHOP_PLAQUE.defaultBlockState());
		if (!LaidPaperBlock.placePage(
			level,
			MATRIX_POS,
			Direction.NORTH,
			new ItemStack(ModItems.VILLAGE_CHRONICLE_MATRIX),
			null
		)) {
			throw new AssertionError("Could not lay a matrix for lens testing");
		}
		if (!LaidPaperBlock.placePage(
			level,
			PRINT_POS,
			Direction.NORTH,
			new ItemStack(ModItems.PRINTED_WARNING_POSTER),
			null
		)) {
			throw new AssertionError("Could not lay a finished print for lens testing");
		}
		assertLaidItem(level, MATRIX_POS, ModItems.VILLAGE_CHRONICLE_MATRIX);
		assertLaidItem(level, PRINT_POS, ModItems.PRINTED_WARNING_POSTER);

		BlockPos support = POSTER_POS.relative(Direction.SOUTH);
		level.setBlockAndUpdate(support, Blocks.SMOOTH_STONE.defaultBlockState());
		level.setBlockAndUpdate(
			POSTER_POS,
			ModBlocks.HANGING_POSTER.defaultBlockState().setValue(HangingPosterBlock.FACING, Direction.NORTH)
		);
	}

	private static void assertLaidItem(ServerLevel level, BlockPos pos, net.minecraft.world.item.Item expected) {
		if (!(level.getBlockEntity(pos) instanceof LaidPaperBlockEntity laid) || !laid.page().is(expected)) {
			throw new AssertionError("Laid item was not stored at " + pos);
		}
	}

	private static void assertLensHandled(
		ServerLevel level,
		ServerPlayer player,
		BlockPos pos,
		Direction face,
		String label
	) {
		ItemStack lens = new ItemStack(ModItems.MAGNIFYING_LENS);
		InteractionResult result = useItemOn(level, player, pos, face, lens);
		if (!result.consumesAction()
			|| !player.isUsingItem()
			|| !player.getUseItem().is(ModItems.MAGNIFYING_LENS)) {
			throw new AssertionError("Magnifying Lens did not handle " + label + ": " + result);
		}
		player.releaseUsingItem();
	}

	private static void equipBrush(ServerPlayer player) {
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.PRINTERS_BRUSH));
		player.inventoryMenu.broadcastChanges();
	}

	private static void assertBrushUsing(ServerPlayer player) {
		if (!player.isUsingItem() || !player.getUseItem().is(ModItems.PRINTERS_BRUSH)) {
			throw new AssertionError("Real right-click hold did not start the Printer's Brush");
		}
	}

	private static InteractionResult useItemOn(
		ServerLevel level,
		ServerPlayer player,
		BlockPos pos,
		Direction face,
		ItemStack stack
	) {
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		return player.gameMode.useItemOn(
			player,
			level,
			stack,
			InteractionHand.MAIN_HAND,
			new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false)
		);
	}

	private static InvestigationBlockEntity requirePlaque(ServerLevel level) {
		if (level.getBlockEntity(PLAQUE_POS) instanceof InvestigationBlockEntity plaque) {
			return plaque;
		}
		throw new AssertionError("Faded Workshop Plaque block entity missing");
	}

	private static void assertPlaqueState(ServerLevel level, InvestigationState expected) {
		InvestigationState actual = level.getBlockState(PLAQUE_POS)
			.getValue(InvestigatableBlock.INVESTIGATION);
		if (actual != expected) {
			throw new AssertionError("Plaque cleaning state was " + actual + ", expected " + expected);
		}
	}

	private static void takeScreenshot(ClientGameTestContext context, String name) {
		var path = context.takeScreenshot(name).toAbsolutePath();
		EchoesInInk.LOGGER.info("Investigation tools ClientGameTest screenshot: {}", path);
	}

	private static ServerPlayer requirePlayer(List<ServerPlayer> players) {
		if (players.isEmpty()) {
			throw new AssertionError("Client GameTest server has no player");
		}
		return players.getFirst();
	}
}
