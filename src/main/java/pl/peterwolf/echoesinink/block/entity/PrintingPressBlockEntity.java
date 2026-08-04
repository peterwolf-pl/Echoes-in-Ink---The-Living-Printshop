package pl.peterwolf.echoesinink.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.archive.ArchiveEntries;
import pl.peterwolf.echoesinink.archive.ArchiveService;
import pl.peterwolf.echoesinink.block.PressPhase;
import pl.peterwolf.echoesinink.block.PrintingPressBlock;
import pl.peterwolf.echoesinink.config.ModConfig;
import pl.peterwolf.echoesinink.item.ModItems;
import pl.peterwolf.echoesinink.recipe.PrintingRecipe;
import pl.peterwolf.echoesinink.recipe.PrintingRecipes;
import pl.peterwolf.echoesinink.progression.PrintshopProgressionSavedData;
import pl.peterwolf.echoesinink.sound.ModSounds;

/**
 * Server-authoritative screw press. Client only reads phase + progress for animation.
 * Implements {@link WorldlyContainer} with no hopper-accessible faces so automation
 * cannot skip the physical print sequence or extract mid-cycle.
 */
public class PrintingPressBlockEntity extends BlockEntity implements WorldlyContainer {
	public static final int SLOT_MATRIX = 0;
	public static final int SLOT_INK = 1;
	public static final int SLOT_PAPER = 2;
	public static final int SLOT_OUTPUT = 3;
	public static final int SLOT_COUNT = 4;
	public static final int INKING_DURATION_TICKS = 32;
	private static final int[] NO_SLOTS = new int[0];

	private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

	private boolean hasScrew;
	private boolean hasHandle;
	private boolean hasPlaten;
	private boolean hasCarriage;

	private PressPhase phase = PressPhase.INCOMPLETE;
	private int progress;
	private int maxProgress = 60;
	/** 0..1 visual blend; advanced on server during PRESSING/RESETTING. */
	private float animProgress;
	/** True only after the current matrix has received ink and before impression. */
	private boolean matrixInked;

	public PrintingPressBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PRINTING_PRESS, pos, state);
	}

	// ── Accessors (client + server) ────────────────────────────────────────

	public PressPhase phase() {
		return phase;
	}

	public float animProgress() {
		return animProgress;
	}

	public boolean isFullyAssembled() {
		return hasScrew && hasHandle && hasPlaten && hasCarriage;
	}

	public boolean hasScrew() { return hasScrew; }
	public boolean hasHandle() { return hasHandle; }
	public boolean hasPlaten() { return hasPlaten; }
	public boolean hasCarriage() { return hasCarriage; }

	public int progress() { return progress; }
	public int maxProgress() { return maxProgress; }
	public boolean matrixInked() { return matrixInked; }

	// ── Assembly ───────────────────────────────────────────────────────────

	public boolean tryInstallPart(Player player, ItemStack stack) {
		if (level == null || level.isClientSide() || stack.isEmpty()) {
			return false;
		}
		boolean changed = false;
		if (stack.getItem() == ModItems.PRESS_SCREW && !hasScrew) {
			hasScrew = true;
			changed = true;
		} else if (stack.getItem() == ModItems.PRESS_HANDLE && !hasHandle) {
			hasHandle = true;
			changed = true;
		} else if (stack.getItem() == ModItems.PRESS_PLATEN && !hasPlaten) {
			hasPlaten = true;
			changed = true;
		} else if (stack.getItem() == ModItems.PRESS_CARRIAGE && !hasCarriage) {
			hasCarriage = true;
			changed = true;
		}
		if (!changed) {
			return false;
		}
		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		if (isFullyAssembled() && phase == PressPhase.INCOMPLETE) {
			phase = PressPhase.IDLE;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			if (hasScrew) ArchiveService.unlock(serverPlayer, ArchiveEntries.PART_SCREW);
			if (hasHandle) ArchiveService.unlock(serverPlayer, ArchiveEntries.PART_HANDLE);
			if (hasPlaten) ArchiveService.unlock(serverPlayer, ArchiveEntries.PART_PLATEN);
			if (hasCarriage) ArchiveService.unlock(serverPlayer, ArchiveEntries.PART_CARRIAGE);
			if (isFullyAssembled()) {
				ArchiveService.unlock(serverPlayer, ArchiveEntries.WORKSHOP_ASHEN);
			}
		}
		play(ModSounds.PRESS_ASSEMBLE, 0.6F, 1.2F);
		sync();
		return true;
	}

	/** Dev/helper: instantly complete assembly without consuming items. */
	public void forceAssemble() {
		hasScrew = hasHandle = hasPlaten = hasCarriage = true;
		if (phase == PressPhase.INCOMPLETE || phase == PressPhase.JAMMED) {
			phase = PressPhase.IDLE;
		}
		progress = 0;
		animProgress = 0.0F;
		sync();
	}

	/** Human-readable next action for the action bar. */
	public Component nextStepMessage() {
		if (!isFullyAssembled()) {
			StringBuilder missing = new StringBuilder();
			if (!hasScrew) missing.append("screw ");
			if (!hasPlaten) missing.append("platen ");
			if (!hasHandle) missing.append("handle ");
			if (!hasCarriage) missing.append("carriage ");
			return Component.translatable("press.echoes_in_ink.next.install", missing.toString().trim());
		}
		return switch (phase) {
			case IDLE -> {
				if (items.get(SLOT_MATRIX).isEmpty()) {
					yield Component.translatable("press.echoes_in_ink.next.matrix");
				}
				if (items.get(SLOT_INK).isEmpty()) {
					yield Component.translatable("press.echoes_in_ink.next.ink_or_swap");
				}
				if (items.get(SLOT_PAPER).isEmpty()) {
					yield Component.translatable("press.echoes_in_ink.next.paper_or_swap");
				}
				if (PrintingRecipes.findMatch(
					items.get(SLOT_MATRIX),
					items.get(SLOT_PAPER),
					items.get(SLOT_INK)
				).isEmpty()) {
					yield Component.translatable("press.echoes_in_ink.next.incompatible");
				}
				yield Component.translatable("press.echoes_in_ink.next.carriage");
			}
			case INKING -> Component.translatable("press.echoes_in_ink.next.inking");
			case CARRIAGE_IN -> Component.translatable("press.echoes_in_ink.next.handle");
			case PRESSING -> Component.translatable("press.echoes_in_ink.next.wait");
			case RESETTING -> Component.translatable("press.echoes_in_ink.next.wait");
			case IMPRESSION_DONE -> Component.translatable("press.echoes_in_ink.next.pull_carriage");
			case OUTPUT_READY -> Component.translatable("press.echoes_in_ink.next.collect");
			case JAMMED -> Component.translatable("press.echoes_in_ink.next.clear_jam");
			default -> Component.translatable("press.echoes_in_ink.next.install", "parts");
		};
	}

	// ── Input insertion ────────────────────────────────────────────────────

	public boolean tryInsertInput(Player player, ItemStack stack) {
		if (level == null || level.isClientSide() || !isFullyAssembled()) {
			return false;
		}
		if (phase != PressPhase.IDLE && phase != PressPhase.OUTPUT_READY) {
			return false;
		}
		// Don't accept inputs while output is waiting.
		if (phase == PressPhase.OUTPUT_READY) {
			return false;
		}

		// Matrix swap: hold a new matrix against a loaded drawer to replace it.
		if (isMatrixItem(stack) && !items.get(SLOT_MATRIX).isEmpty()) {
			ItemStack old = items.get(SLOT_MATRIX);
			items.set(SLOT_MATRIX, stack.copyWithCount(1));
			matrixInked = false;
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
			giveOrDrop(player, old);
			unlockMatrix(player, items.get(SLOT_MATRIX));
			if (canStartInking()) {
				startInking();
			} else {
				play(ModSounds.PRESS_LOAD, 0.7F, 0.85F);
				sync();
			}
			return true;
		}

		int slot = -1;
		if (isMatrixItem(stack) && items.get(SLOT_MATRIX).isEmpty()) {
			slot = SLOT_MATRIX;
		} else if (isInkItem(stack) && items.get(SLOT_INK).isEmpty()) {
			slot = SLOT_INK;
		} else if (isPaperItem(stack) && items.get(SLOT_PAPER).isEmpty()) {
			slot = SLOT_PAPER;
		}
		if (slot < 0) {
			return false;
		}
		ItemStack insert = stack.copyWithCount(1);
		items.set(slot, insert);
		if (slot == SLOT_MATRIX || slot == SLOT_INK) {
			matrixInked = false;
		}
		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		if (slot == SLOT_MATRIX) {
			unlockMatrix(player, insert);
		}
		if ((slot == SLOT_MATRIX || slot == SLOT_INK) && canStartInking()) {
			startInking();
		} else {
			play(ModSounds.PRESS_LOAD, 0.7F, 1.0F);
			sync();
		}
		return true;
	}

	private boolean canStartInking() {
		return !items.get(SLOT_MATRIX).isEmpty() && !items.get(SLOT_INK).isEmpty();
	}

	private void startInking() {
		phase = PressPhase.INKING;
		progress = 0;
		maxProgress = INKING_DURATION_TICKS;
		animProgress = 0.0F;
		matrixInked = false;
		play(ModSounds.PRESS_INK, 0.55F, 0.8F);
		sync();
	}

	private static void unlockMatrix(Player player, ItemStack matrix) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		var item = matrix.getItem();
		if (item == ModItems.WOODEN_PRINTING_MATRIX) {
			ArchiveService.unlock(serverPlayer, ArchiveEntries.MATRIX_WOODEN);
		} else if (item == ModItems.METAL_TYPE_PIECE) {
			ArchiveService.unlock(serverPlayer, ArchiveEntries.MATRIX_TYPE);
		} else if (item == ModItems.CHARCOAL_RUBBING) {
			ArchiveService.unlock(serverPlayer, ArchiveEntries.MATRIX_RUBBING);
		} else if (item == ModItems.VILLAGE_CHRONICLE_MATRIX) {
			ArchiveService.unlock(serverPlayer, ArchiveEntries.MATRIX_VILLAGE_CHRONICLE);
		} else if (item == ModItems.FORBIDDEN_NOTICE_FORME) {
			ArchiveService.unlock(serverPlayer, ArchiveEntries.MATRIX_FORBIDDEN_NOTICE);
		}
		ArchiveService.recordRecoveredMaterial(serverPlayer, BuiltInRegistries.ITEM.getKey(item).getPath());
		for (String recipeId : PrintingRecipes.recipeIdsForMatrix(matrix)) {
			ArchiveService.recordAvailableRecipe(serverPlayer, recipeId);
		}
	}

	private static void giveOrDrop(Player player, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false);
		}
	}

	private static boolean isMatrixItem(ItemStack stack) {
		var item = stack.getItem();
		return item == ModItems.WOODEN_PRINTING_MATRIX
			|| item == ModItems.METAL_TYPE_PIECE
			|| item == ModItems.CHARCOAL_RUBBING
			|| item == ModItems.VILLAGE_CHRONICLE_MATRIX
			|| item == ModItems.FORBIDDEN_NOTICE_FORME;
	}

	private static boolean isInkItem(ItemStack stack) {
		var item = stack.getItem();
		return item == ModItems.INK_BALL || item == ModItems.INK_PAD;
	}

	private static boolean isPaperItem(ItemStack stack) {
		var item = stack.getItem();
		return item == ModItems.BLANK_ARCHIVE_PAGE || item == ModItems.DAMAGED_ARCHIVE_PAGE;
	}

	// ── Empty-hand machine actions ─────────────────────────────────────────

	/**
	 * Physical sequence control. Returns a translation key for feedback (may be empty).
	 */
	public String interactEmptyHand(Player player) {
		if (level == null || level.isClientSide()) {
			return "";
		}
		if (!isFullyAssembled()) {
			return "press.echoes_in_ink.incomplete";
		}

		return switch (phase) {
			case INCOMPLETE -> "press.echoes_in_ink.incomplete";
			case IDLE -> tryIdleEmptyHand(player);
			case INKING -> "press.echoes_in_ink.inking";
			case CARRIAGE_IN -> tryPullHandle(player);
			case PRESSING -> "press.echoes_in_ink.pressing";
			case RESETTING -> "press.echoes_in_ink.resetting";
			case IMPRESSION_DONE -> tryPullCarriage(player);
			case OUTPUT_READY -> tryCollectOutput(player);
			case JAMMED -> clearJam(player);
		};
	}

	/**
	 * Idle empty-hand:
	 * <ul>
	 *   <li>Sneak → open drawer (eject matrix, then ink, then paper)</li>
	 *   <li>Full load → push carriage</li>
	 *   <li>Partial load with matrix → eject matrix (change form)</li>
	 *   <li>Otherwise → missing-input feedback</li>
	 * </ul>
	 */
	private String tryIdleEmptyHand(Player player) {
		if (player.isShiftKeyDown()) {
			return tryEjectDrawer(player);
		}
		boolean hasMatrix = !items.get(SLOT_MATRIX).isEmpty();
		boolean hasInk = !items.get(SLOT_INK).isEmpty();
		boolean hasPaper = !items.get(SLOT_PAPER).isEmpty();
		if (hasMatrix && hasInk && hasPaper) {
			return tryPushCarriage(player);
		}
		// Drawer metaphor: incomplete load + empty hand removes the matrix so it can be swapped.
		if (hasMatrix) {
			return ejectSlot(player, SLOT_MATRIX, "press.echoes_in_ink.matrix_removed");
		}
		if (!hasInk && !hasPaper) {
			return "press.echoes_in_ink.missing_matrix";
		}
		if (!hasInk) {
			return "press.echoes_in_ink.missing_ink";
		}
		return "press.echoes_in_ink.missing_paper";
	}

	/** Sneak + empty hand: pull items out of the press drawer (matrix first). */
	private String tryEjectDrawer(Player player) {
		if (!items.get(SLOT_MATRIX).isEmpty()) {
			return ejectSlot(player, SLOT_MATRIX, "press.echoes_in_ink.matrix_removed");
		}
		if (!items.get(SLOT_INK).isEmpty()) {
			return ejectSlot(player, SLOT_INK, "press.echoes_in_ink.ink_removed");
		}
		if (!items.get(SLOT_PAPER).isEmpty()) {
			return ejectSlot(player, SLOT_PAPER, "press.echoes_in_ink.paper_removed");
		}
		return "press.echoes_in_ink.drawer_empty";
	}

	private String ejectSlot(Player player, int slot, String messageKey) {
		ItemStack taken = items.get(slot).copy();
		items.set(slot, ItemStack.EMPTY);
		if (slot == SLOT_MATRIX || slot == SLOT_INK) {
			matrixInked = false;
		}
		giveOrDrop(player, taken);
		play(ModSounds.PRESS_CARRIAGE, 0.45F, 1.15F);
		sync();
		return messageKey;
	}

	private String tryPushCarriage(Player player) {
		if (items.get(SLOT_MATRIX).isEmpty()) {
			return "press.echoes_in_ink.missing_matrix";
		}
		if (items.get(SLOT_INK).isEmpty()) {
			return "press.echoes_in_ink.missing_ink";
		}
		if (items.get(SLOT_PAPER).isEmpty()) {
			return "press.echoes_in_ink.missing_paper";
		}
		if (!items.get(SLOT_OUTPUT).isEmpty()) {
			return "press.echoes_in_ink.output_blocked";
		}
		if (!matrixInked) {
			startInking();
			return "press.echoes_in_ink.inking_started";
		}
		if (PrintingRecipes.findMatch(items.get(SLOT_MATRIX), items.get(SLOT_PAPER), items.get(SLOT_INK)).isEmpty()) {
			// A wrong recipe is a recoverable loading choice, not a mechanical jam.
			// Keep the carriage out and the form inked so the player can open the
			// drawer and replace only the incompatible paper or ink.
			return "press.echoes_in_ink.no_recipe";
		}
		phase = PressPhase.CARRIAGE_IN;
		animProgress = 0.0F;
		play(ModSounds.PRESS_CARRIAGE, 0.5F, 0.8F);
		sync();
		return "press.echoes_in_ink.carriage_in";
	}

	private String tryPullHandle(Player player) {
		var recipeOpt = PrintingRecipes.findMatch(items.get(SLOT_MATRIX), items.get(SLOT_PAPER), items.get(SLOT_INK));
		if (recipeOpt.isEmpty()) {
			phase = PressPhase.JAMMED;
			sync();
			return "press.echoes_in_ink.no_recipe";
		}
		PrintingRecipe recipe = recipeOpt.get();
		maxProgress = Math.max(10, recipe.durationTicks() > 0
			? recipe.durationTicks()
			: ModConfig.INSTANCE.defaultPrintingDurationTicks);
		progress = 0;
		animProgress = 0.0F;
		phase = PressPhase.PRESSING;
		if (level instanceof ServerLevel serverLevel) {
			PrintshopProgressionSavedData.get(serverLevel).markBasicPressOperated();
		}
		if (player instanceof ServerPlayer serverPlayer) {
			ArchiveService.grantAdvancement(serverPlayer, pl.peterwolf.echoesinink.EchoesInInk.id("pull_the_handle"));
			ArchiveService.recordAvailableRecipe(serverPlayer, "press_screw");
			ArchiveService.recordAvailableRecipe(serverPlayer, "press_handle");
			ArchiveService.recordAvailableRecipe(serverPlayer, "press_platen");
			ArchiveService.recordAvailableRecipe(serverPlayer, "press_carriage");
		}
		play(ModSounds.PRESS_WORK, 0.7F, 0.7F);
		sync();
		return "press.echoes_in_ink.handle_pulled";
	}

	private String tryPullCarriage(Player player) {
		phase = PressPhase.OUTPUT_READY;
		animProgress = 1.0F;
		play(ModSounds.PRESS_CARRIAGE, 0.5F, 0.9F);
		sync();
		return "press.echoes_in_ink.carriage_out";
	}

	private String tryCollectOutput(Player player) {
		ItemStack out = items.get(SLOT_OUTPUT);
		if (out.isEmpty()) {
			phase = PressPhase.IDLE;
			sync();
			return "press.echoes_in_ink.empty";
		}
		ItemStack give = out.copy();
		ItemStack printedResult = give.copy();
		items.set(SLOT_OUTPUT, ItemStack.EMPTY);
		if (!player.getInventory().add(give)) {
			player.drop(give, false);
		}
		if (player instanceof ServerPlayer serverPlayer) {
			// Inventory insertion may consume the source stack to count zero. Keep a
			// stable copy so archive progression records the item that was printed.
			unlockPrintResult(serverPlayer, printedResult);
			// First meaningful print can stir The Last Print Run (once until archive entry exists).
			if ((printedResult.getItem() == ModItems.PRINTERS_INSTRUCTION_SHEET
				|| printedResult.getItem() == ModItems.RESTORED_CHRONICLE_PAGE)
				&& !ArchiveService.get(serverPlayer).has(ArchiveEntries.ECHO_LAST_PRINT)
				&& level instanceof ServerLevel serverLevel) {
				pl.peterwolf.echoesinink.echo.EchoManager.startLastPrintRun(
					serverLevel, worldPosition, serverPlayer
				);
			}
		}
		// Matrix stays for re-use; ink and paper already consumed when print finished.
		phase = PressPhase.IDLE;
		animProgress = 0.0F;
		progress = 0;
		play(ModSounds.PRESS_COLLECT, 0.8F, 1.0F);
		sync();
		return "press.echoes_in_ink.collected";
	}

	private static void unlockPrintResult(ServerPlayer player, ItemStack printed) {
		var item = printed.getItem();
		ArchiveService.recordPrintedWork(player, BuiltInRegistries.ITEM.getKey(item).getPath());
		if (item == ModItems.PRINTERS_INSTRUCTION_SHEET) {
			ArchiveService.unlock(player, ArchiveEntries.WORK_INSTRUCTION);
		} else if (item == ModItems.RESTORED_CHRONICLE_PAGE) {
			ArchiveService.unlock(player, ArchiveEntries.WORK_CHRONICLE);
		} else if (item == ModItems.DECORATIVE_WOODCUT) {
			ArchiveService.unlock(player, ArchiveEntries.WORK_WOODCUT);
		} else if (item == ModItems.PRINTED_WARNING_POSTER) {
			ArchiveService.unlock(player, ArchiveEntries.WORK_POSTER);
		} else if (item == ModItems.WORKSHOP_MAP_FRAGMENT) {
			ArchiveService.unlock(player, ArchiveEntries.WORK_MAP);
		} else if (item == ModItems.VILLAGE_CHRONICLE_PRINT) {
			ArchiveService.unlock(player, ArchiveEntries.WORK_VILLAGE_CHRONICLE);
		} else if (item == ModItems.FORBIDDEN_NOTICE_PRINT) {
			ArchiveService.unlock(player, ArchiveEntries.WORK_FORBIDDEN_NOTICE);
		}
	}

	private String clearJam(Player player) {
		phase = isFullyAssembled() ? PressPhase.IDLE : PressPhase.INCOMPLETE;
		progress = 0;
		animProgress = 0.0F;
		sync();
		return "press.echoes_in_ink.jam_cleared";
	}

	// ── Tick ───────────────────────────────────────────────────────────────

	public static void serverTick(Level level, BlockPos pos, BlockState state, PrintingPressBlockEntity be) {
		if (!(level instanceof ServerLevel)) {
			return;
		}
		if (be.phase == PressPhase.INKING) {
			be.progress++;
			be.animProgress = Math.min(1.0F, be.progress / (float) INKING_DURATION_TICKS);
			be.setChanged();
			level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
			if (be.progress >= INKING_DURATION_TICKS) {
				be.phase = PressPhase.IDLE;
				be.progress = 0;
				be.animProgress = 1.0F;
				be.matrixInked = true;
				be.play(ModSounds.PRESS_INK, 0.45F, 1.15F);
				be.sync();
			}
		} else if (be.phase == PressPhase.PRESSING) {
			be.progress++;
			be.animProgress = Math.min(1.0F, be.progress / (float) Math.max(1, be.maxProgress));
			// Sync every tick so clients animate the platen/handle smoothly.
			be.setChanged();
			level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
			if (be.progress >= be.maxProgress) {
				be.finishImpression();
			}
		} else if (be.phase == PressPhase.RESETTING) {
			be.progress++;
			be.animProgress = Math.max(0.0F, 1.0F - be.progress / 20.0F);
			be.setChanged();
			level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
			if (be.progress >= 20) {
				be.phase = PressPhase.IMPRESSION_DONE;
				be.progress = 0;
				be.animProgress = 1.0F;
				be.play(ModSounds.PRESS_WORK, 0.5F, 1.3F);
				be.sync();
			}
		}
	}

	private void finishImpression() {
		var recipeOpt = PrintingRecipes.findMatch(items.get(SLOT_MATRIX), items.get(SLOT_PAPER), items.get(SLOT_INK));
		if (recipeOpt.isEmpty()) {
			phase = PressPhase.JAMMED;
			sync();
			return;
		}
		// Consume ink + paper once; keep matrix for reuse.
		items.get(SLOT_INK).shrink(1);
		if (items.get(SLOT_INK).isEmpty()) {
			items.set(SLOT_INK, ItemStack.EMPTY);
		}
		items.get(SLOT_PAPER).shrink(1);
		if (items.get(SLOT_PAPER).isEmpty()) {
			items.set(SLOT_PAPER, ItemStack.EMPTY);
		}
		items.set(SLOT_OUTPUT, recipeOpt.get().createOutput());
		matrixInked = false;
		phase = PressPhase.RESETTING;
		progress = 0;
		animProgress = 1.0F;
		play(ModSounds.PRESS_IMPRESSION, 0.4F, 1.5F);
		sync();
	}

	// ── Break / drops ──────────────────────────────────────────────────────

	private boolean dropped;

	public void dropAll(Level level, BlockPos pos) {
		if (dropped) {
			return;
		}
		dropped = true;
		Containers.dropContents(level, pos, this);
		if (hasScrew) {
			Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.PRESS_SCREW));
		}
		if (hasHandle) {
			Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.PRESS_HANDLE));
		}
		if (hasPlaten) {
			Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.PRESS_PLATEN));
		}
		if (hasCarriage) {
			Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.PRESS_CARRIAGE));
		}
		hasScrew = hasHandle = hasPlaten = hasCarriage = false;
		matrixInked = false;
		clearContent();
	}

	public Component statusMessage() {
		return Component.translatable("press.echoes_in_ink.status." + phase.getSerializedName());
	}

	// ── Container ──────────────────────────────────────────────────────────

	@Override
	public int getContainerSize() {
		return SLOT_COUNT;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : items) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack result = ContainerHelper.removeItem(items, slot, amount);
		if (!result.isEmpty()) {
			if (slot == SLOT_MATRIX || slot == SLOT_INK) {
				matrixInked = false;
			}
			setChanged();
		}
		return result;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		ItemStack result = ContainerHelper.takeItem(items, slot);
		if (!result.isEmpty() && (slot == SLOT_MATRIX || slot == SLOT_INK)) {
			matrixInked = false;
		}
		return result;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		items.set(slot, stack);
		stack.limitSize(getMaxStackSize(stack));
		if (slot == SLOT_MATRIX || slot == SLOT_INK) {
			matrixInked = false;
		}
		setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		items.clear();
		matrixInked = false;
	}

	/** Players insert via tryInsertInput; hoppers/droppers must not automate the press. */
	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return false;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return NO_SLOTS;
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return false;
	}

	// ── Persistence + sync ─────────────────────────────────────────────────

	private void sync() {
		setChanged();
		if (level != null && !level.isClientSide()) {
			BlockState state = getBlockState();
			if (state.hasProperty(PrintingPressBlock.PHASE)) {
				PressPhase blockPhase = phase == PressPhase.INCOMPLETE ? PressPhase.INCOMPLETE : phase;
				// Collapse less-critical phases into a smaller blockstate set for models.
				level.setBlock(worldPosition, state.setValue(PrintingPressBlock.PHASE, toBlockPhase(phase)), Block.UPDATE_CLIENTS);
			}
			level.sendBlockUpdated(worldPosition, state, getBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	private static PressPhase toBlockPhase(PressPhase phase) {
		return switch (phase) {
			case INCOMPLETE -> PressPhase.INCOMPLETE;
			case PRESSING, RESETTING -> PressPhase.PRESSING;
			case CARRIAGE_IN, IMPRESSION_DONE -> PressPhase.CARRIAGE_IN;
			case OUTPUT_READY -> PressPhase.OUTPUT_READY;
			case JAMMED -> PressPhase.JAMMED;
			default -> PressPhase.IDLE;
		};
	}

	private void play(SoundEvent sound, float volume, float pitch) {
		if (level != null && !level.isClientSide()) {
			level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, volume, pitch);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput tag) {
		super.saveAdditional(tag);
		ContainerHelper.saveAllItems(tag, items);
		tag.putBoolean("HasScrew", hasScrew);
		tag.putBoolean("HasHandle", hasHandle);
		tag.putBoolean("HasPlaten", hasPlaten);
		tag.putBoolean("HasCarriage", hasCarriage);
		tag.putString("Phase", phase.getSerializedName());
		tag.putInt("Progress", progress);
		tag.putInt("MaxProgress", maxProgress);
		tag.putFloat("AnimProgress", animProgress);
		tag.putBoolean("MatrixInked", matrixInked);
	}

	@Override
	protected void loadAdditional(ValueInput tag) {
		super.loadAdditional(tag);
		items.clear();
		ContainerHelper.loadAllItems(tag, items);
		hasScrew = tag.getBooleanOr("HasScrew", false);
		hasHandle = tag.getBooleanOr("HasHandle", false);
		hasPlaten = tag.getBooleanOr("HasPlaten", false);
		hasCarriage = tag.getBooleanOr("HasCarriage", false);
		phase = PressPhase.byName(tag.getStringOr("Phase", PressPhase.INCOMPLETE.getSerializedName()));
		progress = tag.getIntOr("Progress", 0);
		maxProgress = tag.getIntOr("MaxProgress", 60);
		animProgress = tag.getFloatOr("AnimProgress", 0.0F);
		matrixInked = tag.getBooleanOr(
			"MatrixInked",
			phase != PressPhase.INKING
				&& !items.get(SLOT_MATRIX).isEmpty()
				&& !items.get(SLOT_INK).isEmpty()
		);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
