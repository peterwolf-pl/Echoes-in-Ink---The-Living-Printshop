package pl.peterwolf.echoesinink.block.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import pl.peterwolf.echoesinink.archive.ArchiveEntries;
import pl.peterwolf.echoesinink.archive.ArchiveService;
import pl.peterwolf.echoesinink.block.InvestigationData;
import pl.peterwolf.echoesinink.block.InvestigationLoot;
import pl.peterwolf.echoesinink.block.InvestigationState;
import pl.peterwolf.echoesinink.block.InvestigatableBlock;
import pl.peterwolf.echoesinink.config.ModConfig;
import pl.peterwolf.echoesinink.item.ModItems;
import pl.peterwolf.echoesinink.progression.InvestigationRole;
import pl.peterwolf.echoesinink.progression.PrintshopProgressionSavedData;
import pl.peterwolf.echoesinink.progression.RewardStack;
import pl.peterwolf.echoesinink.progression.WorkshopRewardAllocator;
import pl.peterwolf.echoesinink.progression.WorkshopRewardItems;
import pl.peterwolf.echoesinink.progression.WorkshopVariant;
import pl.peterwolf.echoesinink.recipe.PrintingRecipes;

/**
 * Persists investigation progress and ensures loot is allocated at most once.
 * Structure-bound roles use deterministic progression rewards; ordinary placed
 * debris continues to use optional weighted loot.
 */
public class InvestigationBlockEntity extends BlockEntity {
	private boolean lootGenerated;
	private String lastResultId = "";
	private String workshopId = "";
	private String workshopVariant = "";
	private String investigationRole = "";

	public InvestigationBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.INVESTIGATION, pos, state);
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
		if (lootGenerated || !this.workshopId.isBlank() || workshopId == null || workshopId.isBlank()) {
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
			investigationRole
		);
	}

	/** Server-only: advance cleaning and allocate the final reward exactly once. */
	public boolean clean(ServerLevel level, Player player) {
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof InvestigatableBlock block)) {
			return false;
		}
		InvestigationState current = state.getValue(InvestigatableBlock.INVESTIGATION);
		if (!current.canClean()) {
			return false;
		}

		InvestigationState next = current.next();
		level.setBlock(worldPosition, state.setValue(InvestigatableBlock.INVESTIGATION, next), 3);
		setChanged();

		if (next == InvestigationState.FULLY_INVESTIGATED && !lootGenerated) {
			RewardResult result = createReward(level, block);
			lootGenerated = true;
			lastResultId = result.id();
			setChanged();

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
		} else if (next == InvestigationState.FULLY_INVESTIGATED && lootGenerated) {
			player.sendSystemMessage(Component.translatable("investigation.echoes_in_ink.already_searched"));
		}
		return true;
	}

	private RewardResult createReward(ServerLevel level, InvestigatableBlock block) {
		if (!workshopId.isBlank() && !investigationRole.isBlank()) {
			InvestigationRole role = InvestigationRole.byId(investigationRole);
			boolean starter = ModConfig.INSTANCE.starterPrintshopGuaranteesFullPress
				&& PrintshopProgressionSavedData.get(level).claimAndIsStarter(workshopId);
			List<RewardStack> allocation = starter
				? WorkshopRewardAllocator.starter(role, ModConfig.INSTANCE.starterInkImpressions)
				: WorkshopRewardAllocator.later(
					WorkshopVariant.byId(workshopVariant),
					role,
					workshopId,
					ModConfig.INSTANCE.allowSparePressPartsInLaterRuins
				);
			if (!allocation.isEmpty()) {
				String prefix = starter ? "starter" : "later";
				return new RewardResult(
					prefix + ":" + role.id(),
					allocation.stream().map(WorkshopRewardItems::createStack).toList(),
					Component.translatable("investigation.echoes_in_ink." + prefix + "_reward")
				);
			}
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
			|| item == ModItems.UPPER_MATRIX_FRAGMENT
			|| item == ModItems.LOWER_MATRIX_FRAGMENT
			|| item == ModItems.MISSING_LETTER_INSERT
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
			|| item == ModItems.UPPER_MATRIX_FRAGMENT
			|| item == ModItems.LOWER_MATRIX_FRAGMENT
			|| item == ModItems.MISSING_LETTER_INSERT
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
		tag.putString("LastResultId", safe(lastResultId));
		tag.putString("WorkshopId", safe(workshopId));
		tag.putString("WorkshopVariant", safe(workshopVariant));
		tag.putString("InvestigationRole", safe(investigationRole));
	}

	@Override
	protected void loadAdditional(ValueInput tag) {
		super.loadAdditional(tag);
		lootGenerated = tag.getBooleanOr("LootGenerated", false);
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
