package pl.peterwolf.echoesinink.block.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import pl.peterwolf.echoesinink.archive.ArchiveEntries;
import pl.peterwolf.echoesinink.archive.ArchiveService;
import pl.peterwolf.echoesinink.block.InvestigationData;
import pl.peterwolf.echoesinink.block.InvestigationLoot;
import pl.peterwolf.echoesinink.block.InvestigationState;
import pl.peterwolf.echoesinink.block.InvestigatableBlock;
import pl.peterwolf.echoesinink.block.PrintingDebrisBlock;
import pl.peterwolf.echoesinink.config.ModConfig;
import pl.peterwolf.echoesinink.item.ModItems;
import pl.peterwolf.echoesinink.progression.InvestigationRole;
import pl.peterwolf.echoesinink.progression.LegacyWorkshopBinder;
import pl.peterwolf.echoesinink.progression.PrintshopProgressionSavedData;
import pl.peterwolf.echoesinink.progression.RewardStack;
import pl.peterwolf.echoesinink.progression.WorkshopRewardAllocator;
import pl.peterwolf.echoesinink.progression.WorkshopRewardItems;
import pl.peterwolf.echoesinink.progression.WorkshopVariant;
import pl.peterwolf.echoesinink.recipe.PrintingRecipes;

/**
 * Persists investigation progress and ensures loot is allocated at most once.
 * Structure-bound roles use deterministic progression rewards. Printing debris
 * always dismantles into paper, ink, type, and spare press parts, then is
 * removed on the final brush stroke.
 */
public class InvestigationBlockEntity extends BlockEntity {
	private boolean lootGenerated;
	/** Plaque/clue nodes must be inspected with the lens before the brush. */
	private boolean lensInspected;
	private String lastResultId = "";
	private String workshopId = "";
	private String workshopVariant = "";
	private String investigationRole = "";

	public InvestigationBlockEntity(BlockPos pos, BlockState state) {
		this(ModBlockEntities.INVESTIGATION, pos, state);
	}

	protected InvestigationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public boolean isLootGenerated() {
		return lootGenerated;
	}

	public String lastResultId() {
		return lastResultId;
	}

	public String workshopId() {
		return workshopId;
	}

	public String workshopVariant() {
		return workshopVariant;
	}

	public String investigationRole() {
		return investigationRole;
	}

	/** Called by structure generation. Once assigned, a node cannot be repurposed. */
	public void configureWorkshop(String workshopId, WorkshopVariant variant, InvestigationRole role) {
		if (!this.workshopId.isBlank() || workshopId == null || workshopId.isBlank()) {
			return;
		}
		this.workshopId = workshopId;
		this.workshopVariant = variant == null ? WorkshopVariant.RURAL_WOODCUT.id() : variant.id();
		this.investigationRole = role == null ? InvestigationRole.SUSPICIOUS_FLOOR.id() : role.id();
		setChanged();
	}

	public void applyFromItemData(InvestigationData data) {
		if (data == null) {
			return;
		}
		lootGenerated = data.lootGenerated();
		lensInspected = data.lensInspected();
		lastResultId = safe(data.lastResultId());
		workshopId = safe(data.workshopId());
		workshopVariant = safe(data.workshopVariant());
		investigationRole = safe(data.investigationRole());
		setChanged();
	}

	public InvestigationData toItemData(BlockState state) {
		InvestigationState investigation = state.hasProperty(InvestigatableBlock.INVESTIGATION)
			? state.getValue(InvestigatableBlock.INVESTIGATION)
			: InvestigationState.UNTOUCHED;
		return InvestigationData.of(
			lootGenerated,
			lastResultId,
			investigation,
			workshopId,
			workshopVariant,
			investigationRole,
			lensInspected
		);
	}

	public boolean isLensInspected() {
		return lensInspected;
	}

	/** Called when the player inspects this block with the magnifying lens. */
	public void markLensInspected(Player player) {
		if (lensInspected) {
			return;
		}
		lensInspected = true;
		setChanged();
		if (player instanceof ServerPlayer serverPlayer
			&& getBlockState().getBlock() instanceof pl.peterwolf.echoesinink.block.FadedWorkshopPlaqueBlock) {
			serverPlayer.sendOverlayMessage(
				Component.translatable("investigation.echoes_in_ink.plaque_inspected")
			);
		}
	}

	private boolean requiresLensBeforeBrush() {
		return getBlockState().getBlock() instanceof pl.peterwolf.echoesinink.block.FadedWorkshopPlaqueBlock
			|| InvestigationRole.PLAQUE_CLUE.id().equals(investigationRole);
	}

	/** Server-only: advance cleaning and allocate the final reward exactly once. */
	public boolean clean(ServerLevel level, Player player) {
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof InvestigatableBlock block)) {
			return false;
		}
		InvestigationState current = state.getValue(InvestigatableBlock.INVESTIGATION);
		if (!current.canClean()) {
			return recoverFullySearchedLegacyWorkshop(level, player, current);
		}

		// Plaque: lens first, then brush twice (untouched → partial → fully).
		if (requiresLensBeforeBrush() && !lensInspected) {
			if (player instanceof ServerPlayer serverPlayer) {
				serverPlayer.sendOverlayMessage(Component.translatable("investigation.echoes_in_ink.need_lens_first"));
			} else {
				player.sendSystemMessage(Component.translatable("investigation.echoes_in_ink.need_lens_first"));
			}
			return false;
		}

		if (block instanceof PrintingDebrisBlock) {
			return cleanDebris(level, player, block, state, current);
		}

		InvestigationState next = current.next();
		level.setBlock(worldPosition, state.setValue(InvestigatableBlock.INVESTIGATION, next), 3);
		setChanged();

		if (next == InvestigationState.FULLY_INVESTIGATED && !lootGenerated) {
			RewardResult result = createReward(level, block);
			lootGenerated = true;
			lastResultId = result.id();
			setChanged();
			deliverReward(level, player, result);
		} else if (next == InvestigationState.FULLY_INVESTIGATED && lootGenerated) {
			player.sendSystemMessage(Component.translatable("investigation.echoes_in_ink.already_searched"));
		}
		return true;
	}

	/**
	 * Printing debris falls apart while brushed. The first stroke sheds scraps;
	 * the second grants any role reward, dumps the remaining materials, and
	 * removes the pile.
	 */
	private boolean cleanDebris(
		ServerLevel level,
		Player player,
		InvestigatableBlock block,
		BlockState state,
		InvestigationState current
	) {
		InvestigationState next = current.next();
		if (next == InvestigationState.PARTIALLY_CLEANED) {
			level.setBlock(worldPosition, state.setValue(InvestigatableBlock.INVESTIGATION, next), 3);
			setChanged();
			deliverReward(
				level,
				player,
				new RewardResult(
					"debris_partial",
					InvestigationLoot.dismantlePartial(level.getRandom()),
					Component.translatable("investigation.echoes_in_ink.debris_partial")
				)
			);
			return true;
		}

		if (lootGenerated) {
			player.sendSystemMessage(Component.translatable("investigation.echoes_in_ink.already_searched"));
			return true;
		}

		RewardResult role = createReward(level, block);
		List<ItemStack> stacks = new ArrayList<>(role.stacks());
		stacks.addAll(InvestigationLoot.dismantleComplete(level.getRandom()));
		boolean roleBound = role.id().startsWith("starter:") || role.id().startsWith("later:");
		RewardResult result = new RewardResult(
			roleBound ? role.id() : "debris_dismantle",
			List.copyOf(stacks),
			roleBound
				? role.message()
				: Component.translatable("investigation.echoes_in_ink.debris_dismantled")
		);
		lootGenerated = true;
		lastResultId = result.id();
		setChanged();
		deliverReward(level, player, result);
		level.playSound(null, worldPosition, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.8F, 0.9F);
		level.sendParticles(
			new BlockParticleOption(ParticleTypes.BLOCK, state),
			worldPosition.getX() + 0.5,
			worldPosition.getY() + 0.35,
			worldPosition.getZ() + 0.5,
			18,
			0.3, 0.2, 0.3,
			0.06
		);
		level.removeBlock(worldPosition, false);
		return true;
	}

	private boolean recoverFullySearchedLegacyWorkshop(
		ServerLevel level,
		Player player,
		InvestigationState current
	) {
		if (current != InvestigationState.FULLY_INVESTIGATED
			|| (!workshopId.isBlank() && !investigationRole.isBlank())) {
			return false;
		}
		LegacyWorkshopBinder.MigrationResult migration = LegacyWorkshopBinder.bind(level, worldPosition);
		if (!migration.migrated()) {
			return false;
		}
		if (!ModConfig.INSTANCE.starterPrintshopGuaranteesFullPress
			|| !PrintshopProgressionSavedData.get(level).starterRewardsAllowed(workshopId)
			|| migration.compensationRoles().isEmpty()) {
			return true;
		}

		List<RewardStack> allocation = new java.util.ArrayList<>();
		for (InvestigationRole role : migration.compensationRoles()) {
			allocation.addAll(WorkshopRewardAllocator.starter(
				role,
				ModConfig.INSTANCE.starterInkImpressions
			));
		}
		RewardResult result = new RewardResult(
			"starter:legacy_compensation",
			allocation.stream().map(WorkshopRewardItems::createStack).toList(),
			Component.translatable("investigation.echoes_in_ink.starter_reward")
		);
		lastResultId = result.id();
		setChanged();
		deliverReward(level, player, result);
		return true;
	}

	private void deliverReward(ServerLevel level, Player player, RewardResult result) {
		for (ItemStack stack : result.stacks()) {
			giveOrDrop(level, player, stack.copy());
		}
		player.sendSystemMessage(result.message());
		if (player instanceof ServerPlayer serverPlayer) {
			if (!workshopId.isBlank()) {
				ArchiveService.recordWorkshop(serverPlayer, workshopId, workshopVariant);
			}
			ArchiveService.unlock(serverPlayer, ArchiveEntries.CLUE_DUST);
			ArchiveService.unlock(serverPlayer, ArchiveEntries.WORKSHOP_ASHEN);
			for (ItemStack stack : result.stacks()) {
				unlockForItem(serverPlayer, stack);
			}
			if (result.id().contains("cellar") || result.id().contains("floor_cache")) {
				ArchiveService.unlock(serverPlayer, ArchiveEntries.CLUE_HIDDEN);
			}
			if (result.id().contains("plaque")) {
				ArchiveService.unlock(serverPlayer, ArchiveEntries.CLUE_PLAQUE);
			}
		}
	}

	private RewardResult createReward(ServerLevel level, InvestigatableBlock block) {
		LegacyWorkshopBinder.MigrationResult migration = LegacyWorkshopBinder.MigrationResult.NONE;
		if (workshopId.isBlank() || investigationRole.isBlank()) {
			migration = LegacyWorkshopBinder.bind(level, worldPosition);
		}
		if (!workshopId.isBlank() && !investigationRole.isBlank()) {
			InvestigationRole role = InvestigationRole.byId(investigationRole);
			boolean starter = ModConfig.INSTANCE.starterPrintshopGuaranteesFullPress
				&& PrintshopProgressionSavedData.get(level).starterRewardsAllowed(workshopId);
			List<RewardStack> allocation = new java.util.ArrayList<>(starter
				? WorkshopRewardAllocator.starter(role, ModConfig.INSTANCE.starterInkImpressions)
				: WorkshopRewardAllocator.later(
					WorkshopVariant.byId(workshopVariant),
					role,
					workshopId,
					ModConfig.INSTANCE.allowSparePressPartsInLaterRuins
				));
			if (starter) {
				for (InvestigationRole compensatedRole : migration.compensationRoles()) {
					allocation.addAll(WorkshopRewardAllocator.starter(
						compensatedRole,
						ModConfig.INSTANCE.starterInkImpressions
					));
				}
			}
			if (!allocation.isEmpty()) {
				String prefix = starter ? "starter" : "later";
				return new RewardResult(
					prefix + ":" + role.id(),
					allocation.stream().map(WorkshopRewardItems::createStack).toList(),
					Component.translatable("investigation.echoes_in_ink." + prefix + "_reward")
				);
			}
		}

		if (block instanceof PrintingDebrisBlock || block.lootProfile() == InvestigationLoot.Profile.DEBRIS) {
			return new RewardResult(
				"debris_dismantle",
				List.of(),
				Component.translatable("investigation.echoes_in_ink.debris_dismantled")
			);
		}

		InvestigationLoot.Result random = InvestigationLoot.roll(level, block.lootProfile());
		ItemStack stack = random.createStack();
		return new RewardResult(
			random.id(),
			stack.isEmpty() ? List.of() : List.of(stack),
			random.message()
		);
	}

	private static void giveOrDrop(ServerLevel level, Player player, ItemStack stack) {
		if (stack.isEmpty() || player.getInventory().add(stack)) {
			return;
		}
		ItemEntity drop = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), stack);
		drop.setDefaultPickUpDelay();
		level.addFreshEntity(drop);
	}

	private static void unlockForItem(ServerPlayer player, ItemStack stack) {
		var item = stack.getItem();
		boolean matrixMaterial = item == ModItems.WOODEN_PRINTING_MATRIX
			|| item == ModItems.METAL_TYPE_PIECE
			|| item == ModItems.CHARCOAL_RUBBING
			|| item == ModItems.VILLAGE_CHRONICLE_MATRIX
			|| item == ModItems.LEAD_TYPE_SET
			|| item == ModItems.IRON_CHASE
			|| item == ModItems.MISSING_HEADLINE_TYPE
			|| item == ModItems.PRINTERS_NOTES
			|| item == ModItems.FORBIDDEN_NOTICE_FORME;
		if (matrixMaterial) {
			ArchiveService.recordRecoveredMaterial(
				player,
				net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath()
			);
		}
		if (item == ModItems.PRESS_SCREW) {
			ArchiveService.unlock(player, ArchiveEntries.PART_SCREW);
		} else if (item == ModItems.PRESS_HANDLE) {
			ArchiveService.unlock(player, ArchiveEntries.PART_HANDLE);
		} else if (item == ModItems.PRESS_PLATEN) {
			ArchiveService.unlock(player, ArchiveEntries.PART_PLATEN);
		} else if (item == ModItems.PRESS_CARRIAGE) {
			ArchiveService.unlock(player, ArchiveEntries.PART_CARRIAGE);
		} else if (item == ModItems.WOODEN_PRINTING_MATRIX
			|| item == ModItems.VILLAGE_CHRONICLE_MATRIX) {
			ArchiveService.unlock(player, ArchiveEntries.MATRIX_WOODEN);
		} else if (item == ModItems.METAL_TYPE_PIECE
			|| item == ModItems.LEAD_TYPE_SET
			|| item == ModItems.IRON_CHASE
			|| item == ModItems.MISSING_HEADLINE_TYPE
			|| item == ModItems.PRINTERS_NOTES
			|| item == ModItems.FORBIDDEN_NOTICE_FORME) {
			ArchiveService.unlock(player, ArchiveEntries.MATRIX_TYPE);
		}
		if (item == ModItems.VILLAGE_CHRONICLE_MATRIX) {
			ArchiveService.unlock(player, ArchiveEntries.MATRIX_VILLAGE_CHRONICLE);
		} else if (item == ModItems.FORBIDDEN_NOTICE_FORME) {
			ArchiveService.unlock(player, ArchiveEntries.MATRIX_FORBIDDEN_NOTICE);
		}
		for (String recipeId : PrintingRecipes.recipeIdsForMatrix(stack)) {
			ArchiveService.recordAvailableRecipe(player, recipeId);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput tag) {
		super.saveAdditional(tag);
		tag.putBoolean("LootGenerated", lootGenerated);
		tag.putBoolean("LensInspected", lensInspected);
		tag.putString("LastResultId", safe(lastResultId));
		tag.putString("WorkshopId", safe(workshopId));
		tag.putString("WorkshopVariant", safe(workshopVariant));
		tag.putString("InvestigationRole", safe(investigationRole));
	}

	@Override
	protected void loadAdditional(ValueInput tag) {
		super.loadAdditional(tag);
		lootGenerated = tag.getBooleanOr("LootGenerated", false);
		lensInspected = tag.getBooleanOr("LensInspected", false);
		lastResultId = tag.getStringOr("LastResultId", "");
		workshopId = tag.getStringOr("WorkshopId", "");
		workshopVariant = tag.getStringOr("WorkshopVariant", "");
		investigationRole = tag.getStringOr("InvestigationRole", "");
	}

	private static String safe(String value) {
		return value == null ? "" : value;
	}

	private record RewardResult(String id, List<ItemStack> stacks, Component message) {}
}
