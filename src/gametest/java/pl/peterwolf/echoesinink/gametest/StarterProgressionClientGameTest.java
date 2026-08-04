package pl.peterwolf.echoesinink.gametest;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.archive.ArchiveEntries;
import pl.peterwolf.echoesinink.archive.ArchiveService;
import pl.peterwolf.echoesinink.block.InvestigationData;
import pl.peterwolf.echoesinink.block.InvestigationState;
import pl.peterwolf.echoesinink.block.ModBlocks;
import pl.peterwolf.echoesinink.block.PressPhase;
import pl.peterwolf.echoesinink.block.PrintingPressBlock;
import pl.peterwolf.echoesinink.block.InvestigatableBlock;
import pl.peterwolf.echoesinink.block.entity.InvestigationBlockEntity;
import pl.peterwolf.echoesinink.block.entity.PrintingPressBlockEntity;
import pl.peterwolf.echoesinink.item.ModItems;
import pl.peterwolf.echoesinink.progression.InvestigationRole;
import pl.peterwolf.echoesinink.progression.PrintshopProgressionSavedData;
import pl.peterwolf.echoesinink.progression.RewardKind;
import pl.peterwolf.echoesinink.progression.RewardStack;
import pl.peterwolf.echoesinink.progression.WorkshopLayoutPlan;
import pl.peterwolf.echoesinink.progression.WorkshopRewardAllocator;
import pl.peterwolf.echoesinink.progression.WorkshopRewardItems;
import pl.peterwolf.echoesinink.progression.WorkshopVariant;
import pl.peterwolf.echoesinink.recipe.PrintingRecipes;

/** Proves the first-print loop on a real integrated server using starter rewards only. */
public final class StarterProgressionClientGameTest implements FabricClientGameTest {
	private static final BlockPos PRESS_POS = new BlockPos(0, 4, 0);
	private static final BlockPos REPLACED_FLOOR_POS = new BlockPos(3, 4, 0);

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
			.setUseConsistentSettings(true)
			.create()) {
			TestServerContext serverContext = singleplayer.getServer();
			singleplayer.getClientLevel().waitForChunksRender();

			serverContext.runOnServer(server -> {
				ServerPlayer player = requirePlayer(server.getPlayerList().getPlayers());
				var level = server.overworld();
				for (int x = -4; x <= 4; x++) {
					for (int z = -3; z <= 3; z++) {
						level.setBlockAndUpdate(new BlockPos(x, 3, z), Blocks.SMOOTH_STONE.defaultBlockState());
					}
				}
				level.setBlockAndUpdate(
					PRESS_POS,
					ModBlocks.PRINTING_PRESS.defaultBlockState().setValue(PrintingPressBlock.FACING, Direction.NORTH)
				);
				verifyFullySearchedLegacyRecovery(level, player);
				player.getInventory().clearContent();
				harvestStarterWorkshop(level, player);
				assertInventoryContainsStarterParts(player);

				PrintingPressBlockEntity press = requirePress(level);
				Map<RewardKind, Integer> starter = starterInventory();
				install(press, player, starter, RewardKind.PRESS_SCREW);
				install(press, player, starter, RewardKind.PRESS_HANDLE);
				install(press, player, starter, RewardKind.PRESS_PLATEN);
				install(press, player, starter, RewardKind.PRESS_CARRIAGE);
				if (!press.isFullyAssembled() || press.phase() != PressPhase.IDLE) {
					throw new AssertionError("Starter rewards did not assemble the complete press");
				}

				insert(press, player, stack(starter, RewardKind.WOODEN_MATRIX), "starter wooden matrix");
				insert(press, player, stack(starter, RewardKind.INK_BALL), "starter ink");
				if (press.phase() != PressPhase.INKING) {
					throw new AssertionError("Starter ink did not begin charging the matrix");
				}
				assertSpecialistPrintingRecipes();

				verifyBreakPlaceProtection(level);
			});

			context.waitTicks(40);
			serverContext.runOnServer(server -> {
				ServerPlayer player = requirePlayer(server.getPlayerList().getPlayers());
				PrintingPressBlockEntity press = requirePress(server.overworld());
				Map<RewardKind, Integer> starter = starterInventory();
				insert(press, player, stack(starter, RewardKind.DAMAGED_PAGE), "starter damaged page");
				press.interactEmptyHand(player);
				if (press.phase() != PressPhase.CARRIAGE_IN) {
					throw new AssertionError("Starter materials did not form the chronicle recipe");
				}
				press.interactEmptyHand(player);
				if (press.phase() != PressPhase.PRESSING) {
					throw new AssertionError("Starter press did not begin the first impression");
				}
				if (!PrintshopProgressionSavedData.get(server.overworld()).basicPressOperated()) {
					throw new AssertionError("A real handle pull did not close starter reward mode");
				}
				verifyLaterWorkshopSwitch(server.overworld(), player);
			});

			context.waitTicks(110);
			serverContext.runOnServer(server -> {
				ServerPlayer player = requirePlayer(server.getPlayerList().getPlayers());
				PrintingPressBlockEntity press = requirePress(server.overworld());
				if (press.phase() != PressPhase.IMPRESSION_DONE) {
					throw new AssertionError("Starter chronicle impression did not finish: " + press.phase());
				}
				press.interactEmptyHand(player);
				if (!press.getItem(PrintingPressBlockEntity.SLOT_OUTPUT).is(ModItems.RESTORED_CHRONICLE_PAGE)) {
					throw new AssertionError("Starter loop did not produce the Restored Chronicle Page");
				}
				press.interactEmptyHand(player);
				var archive = ArchiveService.get(player);
				if (!archive.has(ArchiveEntries.WORK_CHRONICLE)) {
					throw new AssertionError("First meaningful print did not unlock the chronicle archive entry");
				}
				for (String replacement : new String[] {"press_screw", "press_handle", "press_platen", "press_carriage"}) {
					if (!archive.availableRecipes().contains(replacement)) {
						throw new AssertionError("Missing multiplayer replacement recipe tracking: " + replacement);
					}
				}
				EchoesInInk.LOGGER.info("STARTER_LOOP_GAMETEST_OK output=restored_chronicle_page replacements=4");
			});
		}
	}

	private static Map<RewardKind, Integer> starterInventory() {
		Map<RewardKind, Integer> result = new EnumMap<>(RewardKind.class);
		for (var role : WorkshopLayoutPlan.roles(WorkshopVariant.RURAL_WOODCUT, 0, 4)) {
			for (RewardStack reward : WorkshopRewardAllocator.starter(role, 5)) {
				result.merge(reward.kind(), reward.count(), Integer::sum);
			}
		}
		return result;
	}

	private static void harvestStarterWorkshop(
		net.minecraft.server.level.ServerLevel level,
		ServerPlayer player
	) {
		InvestigationRole[] roles = {
			InvestigationRole.PRESS_FRAME,
			InvestigationRole.MACHINE_REMAINS,
			InvestigationRole.CELLAR_CACHE,
			InvestigationRole.FLOOR_CACHE,
			InvestigationRole.MATRIX_BENCH,
			InvestigationRole.ARCHIVE_DESK,
			InvestigationRole.INK_STATION,
			InvestigationRole.PLAQUE_CLUE
		};
		var blocks = new net.minecraft.world.level.block.Block[] {
			ModBlocks.BROKEN_PRESS_FRAME,
			ModBlocks.PRINTING_DEBRIS,
			ModBlocks.PRINTING_DEBRIS,
			ModBlocks.HIDDEN_FLOOR_COMPARTMENT,
			ModBlocks.DUSTY_PRINTING_TABLE,
			ModBlocks.DAMAGED_ARCHIVE_SHELF,
			ModBlocks.COLLAPSED_TYPE_CABINET,
			ModBlocks.FADED_WORKSHOP_PLAQUE
		};
		for (int i = 0; i < roles.length; i++) {
			BlockPos pos = new BlockPos(-4 + i, 4, 2);
			level.setBlockAndUpdate(pos, blocks[i].defaultBlockState());
			if (!(level.getBlockEntity(pos) instanceof InvestigationBlockEntity investigation)) {
				throw new AssertionError("Starter investigation node missing at " + pos);
			}
			investigation.configureWorkshop("starter_reward_path", WorkshopVariant.RURAL_WOODCUT, roles[i]);
			investigation.clean(level, player);
			investigation.clean(level, player);
			if (!investigation.isLootGenerated() || !investigation.lastResultId().startsWith("starter:")) {
				throw new AssertionError("Real cleaning path missed starter reward for " + roles[i]);
			}
		}
	}

	private static void assertInventoryContainsStarterParts(ServerPlayer player) {
		for (var item : List.of(
			ModItems.PRESS_SCREW,
			ModItems.PRESS_HANDLE,
			ModItems.PRESS_PLATEN,
			ModItems.PRESS_CARRIAGE
		)) {
			if (player.getInventory().countItem(item) < 1) {
				throw new AssertionError("Real investigation did not grant " + item);
			}
		}
	}

	private static void verifyLaterWorkshopSwitch(
		net.minecraft.server.level.ServerLevel level,
		ServerPlayer player
	) {
		BlockPos pos = new BlockPos(4, 4, -2);
		level.setBlockAndUpdate(pos, ModBlocks.BROKEN_PRESS_FRAME.defaultBlockState());
		if (!(level.getBlockEntity(pos) instanceof InvestigationBlockEntity investigation)) {
			throw new AssertionError("Later-workshop node missing");
		}
		investigation.configureWorkshop(
			"after_press_workshop",
			WorkshopVariant.RURAL_WOODCUT,
			InvestigationRole.PRESS_FRAME
		);
		investigation.clean(level, player);
		investigation.clean(level, player);
		if (!investigation.lastResultId().startsWith("later:")) {
			throw new AssertionError("Later workshop did not switch to specialist rewards");
		}
	}

	private static void verifyFullySearchedLegacyRecovery(
		net.minecraft.server.level.ServerLevel level,
		ServerPlayer player
	) {
		BlockPos base = new BlockPos(40, 4, 40);
		level.getChunk(base.getX() >> 4, base.getZ() >> 4);
		var blocks = new net.minecraft.world.level.block.Block[] {
			ModBlocks.BROKEN_PRESS_FRAME,
			ModBlocks.BROKEN_PRESS_FRAME,
			ModBlocks.DUSTY_PRINTING_TABLE,
			ModBlocks.DAMAGED_ARCHIVE_SHELF,
			ModBlocks.COLLAPSED_TYPE_CABINET,
			ModBlocks.PRINTING_DEBRIS,
			ModBlocks.PRINTING_DEBRIS,
			ModBlocks.FADED_WORKSHOP_PLAQUE
		};
		for (int i = 0; i < blocks.length; i++) {
			BlockPos pos = base.offset(i, 0, 0);
			level.setBlockAndUpdate(
				pos,
				blocks[i].defaultBlockState().setValue(
					InvestigatableBlock.INVESTIGATION,
					InvestigationState.FULLY_INVESTIGATED
				)
			);
			if (!(level.getBlockEntity(pos) instanceof InvestigationBlockEntity investigation)) {
				throw new AssertionError("Legacy node missing at index " + i);
			}
			investigation.applyFromItemData(InvestigationData.of(
				true,
				"legacy:weighted",
				InvestigationState.FULLY_INVESTIGATED
			));
		}
		InvestigationBlockEntity trigger = (InvestigationBlockEntity) level.getBlockEntity(base);
		if (!trigger.clean(level, player)
			|| !trigger.lastResultId().equals("starter:legacy_compensation")) {
			throw new AssertionError("Fully searched legacy printshop did not compensate on revisit");
		}
		for (int i = 0; i < blocks.length; i++) {
			if (!(level.getBlockEntity(base.offset(i, 0, 0)) instanceof InvestigationBlockEntity investigation)
				|| investigation.workshopId().isBlank()
				|| investigation.investigationRole().isBlank()) {
				throw new AssertionError("Legacy node remained unbound at index " + i);
			}
		}
		assertInventoryContainsStarterParts(player);
	}

	private static void install(
		PrintingPressBlockEntity press,
		ServerPlayer player,
		Map<RewardKind, Integer> starter,
		RewardKind kind
	) {
		ItemStack stack = stack(starter, kind);
		if (!press.tryInstallPart(player, stack)) {
			throw new AssertionError("Press rejected starter part " + kind);
		}
	}

	private static ItemStack stack(Map<RewardKind, Integer> starter, RewardKind kind) {
		int count = starter.getOrDefault(kind, 0);
		if (count < 1) {
			throw new AssertionError("Starter allocation missing " + kind);
		}
		return WorkshopRewardItems.createStack(new RewardStack(kind, count));
	}

	private static void insert(PrintingPressBlockEntity press, ServerPlayer player, ItemStack stack, String label) {
		if (!press.tryInsertInput(player, stack)) {
			throw new AssertionError("Press rejected " + label);
		}
	}

	private static void verifyBreakPlaceProtection(net.minecraft.server.level.ServerLevel level) {
		level.setBlockAndUpdate(REPLACED_FLOOR_POS, ModBlocks.LOOSE_INK_STAINED_FLOORBOARDS.defaultBlockState());
		if (!(level.getBlockEntity(REPLACED_FLOOR_POS) instanceof InvestigationBlockEntity original)) {
			throw new AssertionError("Missing original investigation block entity");
		}
		InvestigationData carried = InvestigationData.of(
			true,
			"starter:floor_cache",
			InvestigationState.FULLY_INVESTIGATED,
			"starter_loop_workshop",
			WorkshopVariant.RURAL_WOODCUT.id(),
			"floor_cache"
		);
		original.applyFromItemData(carried);
		level.removeBlock(REPLACED_FLOOR_POS, false);
		level.setBlockAndUpdate(
			REPLACED_FLOOR_POS,
			ModBlocks.LOOSE_INK_STAINED_FLOORBOARDS.defaultBlockState()
				.setValue(pl.peterwolf.echoesinink.block.InvestigatableBlock.INVESTIGATION, carried.investigationState())
		);
		if (!(level.getBlockEntity(REPLACED_FLOOR_POS) instanceof InvestigationBlockEntity replacement)) {
			throw new AssertionError("Missing replacement investigation block entity");
		}
		replacement.applyFromItemData(carried);
		if (!replacement.isLootGenerated() || replacement.getBlockState()
			.getValue(pl.peterwolf.echoesinink.block.InvestigatableBlock.INVESTIGATION).canClean()) {
			throw new AssertionError("Break/place replacement became eligible for a loot reroll");
		}
	}

	private static void assertSpecialistPrintingRecipes() {
		var village = PrintingRecipes.findMatch(
			new ItemStack(ModItems.VILLAGE_CHRONICLE_MATRIX),
			new ItemStack(ModItems.BLANK_ARCHIVE_PAGE),
			new ItemStack(ModItems.INK_BALL)
		).orElseThrow(() -> new AssertionError("Village Chronicle Matrix has no printing recipe"));
		if (!village.createOutput().is(ModItems.VILLAGE_CHRONICLE_PRINT)) {
			throw new AssertionError("Village Chronicle Matrix produces the wrong work");
		}
		var forbidden = PrintingRecipes.findMatch(
			new ItemStack(ModItems.FORBIDDEN_NOTICE_FORME),
			new ItemStack(ModItems.BLANK_ARCHIVE_PAGE),
			new ItemStack(ModItems.INK_PAD)
		).orElseThrow(() -> new AssertionError("Forbidden Notice Forme has no printing recipe"));
		if (!forbidden.createOutput().is(ModItems.FORBIDDEN_NOTICE_PRINT)) {
			throw new AssertionError("Forbidden Notice Forme produces the wrong work");
		}
	}

	private static PrintingPressBlockEntity requirePress(net.minecraft.server.level.ServerLevel level) {
		if (level.getBlockEntity(PRESS_POS) instanceof PrintingPressBlockEntity press) {
			return press;
		}
		throw new AssertionError("Printing press missing at " + PRESS_POS);
	}

	private static ServerPlayer requirePlayer(java.util.List<ServerPlayer> players) {
		if (players.isEmpty()) {
			throw new AssertionError("Singleplayer test started without a server player");
		}
		return players.getFirst();
	}
}
