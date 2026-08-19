package pl.peterwolf.echoesinink.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.block.TypeCabinetBlock;
import pl.peterwolf.echoesinink.item.ModItems;
import pl.peterwolf.echoesinink.sound.ModSounds;

/**
 * Type cabinet storage: four thin drawers, one stack each. Hoppers blocked.
 * Drawer open state lives on the block; openAnim is interpolated for BER.
 */
public class TypeCabinetBlockEntity extends InvestigationBlockEntity implements WorldlyContainer {
	public static final int DRAWER_COUNT = 4;
	public static final int ANIM_TICKS = 8;
	private static final int[] NO_SLOTS = new int[0];

	private final NonNullList<ItemStack> drawers = NonNullList.withSize(DRAWER_COUNT, ItemStack.EMPTY);
	/** 0..1 visual open amount (server advances toward target). */
	private float openAnim;
	private int targetOpenDrawer = -1;

	public TypeCabinetBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TYPE_CABINET, pos, state);
	}

	public NonNullList<ItemStack> drawers() {
		return drawers;
	}

	public float openAnim() {
		return openAnim;
	}

	public int targetOpenDrawer() {
		return targetOpenDrawer;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, TypeCabinetBlockEntity be) {
		int open = state.getValue(TypeCabinetBlock.OPEN_DRAWER);
		be.targetOpenDrawer = open <= 0 ? -1 : open - 1;
		float target = open <= 0 ? 0.0F : 1.0F;
		float step = 1.0F / ANIM_TICKS;
		if (be.openAnim < target) {
			be.openAnim = Math.min(target, be.openAnim + step);
			be.setChanged();
			level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
		} else if (be.openAnim > target) {
			be.openAnim = Math.max(target, be.openAnim - step);
			be.setChanged();
			level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
		}
	}

	public void openDrawer(Level level, BlockPos pos, BlockState state, int drawerIndex) {
		int clamped = Math.max(0, Math.min(DRAWER_COUNT - 1, drawerIndex));
		level.setBlock(pos, state.setValue(TypeCabinetBlock.OPEN_DRAWER, clamped + 1), Block.UPDATE_ALL);
		targetOpenDrawer = clamped;
		level.playSound(null, pos, ModSounds.PRESS_CARRIAGE, SoundSource.BLOCKS, 0.35F, 1.2F);
		setChanged();
	}

	public void closeDrawer(Level level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state.setValue(TypeCabinetBlock.OPEN_DRAWER, 0), Block.UPDATE_ALL);
		targetOpenDrawer = -1;
		level.playSound(null, pos, ModSounds.PRESS_CARRIAGE, SoundSource.BLOCKS, 0.3F, 0.85F);
		setChanged();
	}

	public boolean tryInsert(Player player, ItemStack stack) {
		if (stack.isEmpty() || !canStore(stack)) {
			return false;
		}
		int open = getBlockState().getValue(TypeCabinetBlock.OPEN_DRAWER);
		if (open <= 0) {
			return false;
		}
		int slot = open - 1;
		ItemStack existing = drawers.get(slot);
		if (!existing.isEmpty()) {
			return false;
		}
		ItemStack stored = stack.copyWithCount(1);
		drawers.set(slot, stored);
		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		level.playSound(null, worldPosition, ModSounds.PRESS_LOAD, SoundSource.BLOCKS, 0.4F, 1.1F);
		setChanged();
		sync();
		announceDrawer(player, slot);
		return true;
	}

	public boolean tryExtract(Player player) {
		int open = getBlockState().getValue(TypeCabinetBlock.OPEN_DRAWER);
		if (open <= 0) {
			return false;
		}
		int slot = open - 1;
		ItemStack existing = drawers.get(slot);
		if (existing.isEmpty()) {
			return false;
		}
		ItemStack give = existing.copy();
		drawers.set(slot, ItemStack.EMPTY);
		if (!player.getInventory().add(give)) {
			player.drop(give, false);
		}
		level.playSound(null, worldPosition, ModSounds.PRESS_COLLECT, SoundSource.BLOCKS, 0.4F, 1.0F);
		setChanged();
		sync();
		return true;
	}

	/** Overlay: which drawer is open and what it holds. */
	public void announceDrawer(Player player, int slot) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		int human = slot + 1;
		ItemStack stack = drawers.get(slot);
		if (stack.isEmpty()) {
			serverPlayer.sendOverlayMessage(Component.translatable(
				"block.echoes_in_ink.collapsed_type_cabinet.drawer_empty",
				human
			));
		} else {
			serverPlayer.sendOverlayMessage(Component.translatable(
				"block.echoes_in_ink.collapsed_type_cabinet.drawer_holds",
				human,
				stack.getHoverName()
			));
		}
	}

	public void announceOpenDrawer(Player player) {
		int open = getBlockState().getValue(TypeCabinetBlock.OPEN_DRAWER);
		if (open > 0) {
			announceDrawer(player, open - 1);
		}
	}

	public void dropContents(Level level, BlockPos pos) {
		Containers.dropContents(level, pos, this);
		drawers.clear();
		setChanged();
	}

	private void sync() {
		if (level != null && !level.isClientSide()) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
		}
	}

	/** Client must receive drawer stacks for BER visibility. */
	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static boolean canStore(ItemStack stack) {
		var item = stack.getItem();
		return item == Items.PAPER
			|| item == ModItems.BLANK_ARCHIVE_PAGE
			|| item == ModItems.DAMAGED_ARCHIVE_PAGE
			|| item == ModItems.WOODEN_PRINTING_MATRIX
			|| item == ModItems.VILLAGE_CHRONICLE_MATRIX
			|| item == ModItems.FORBIDDEN_NOTICE_FORME
			|| item == ModItems.METAL_TYPE_PIECE
			|| item == ModItems.LEAD_TYPE_SET
			|| item == ModItems.IRON_CHASE
			|| item == ModItems.MISSING_HEADLINE_TYPE
			|| item == ModItems.CHARCOAL_RUBBING
			|| item == ModItems.CHARCOAL_RUBBING_PAPER
			|| item == ModItems.PRINTERS_NOTES
			|| item == ModItems.INK_BALL
			|| item == ModItems.INK_PAD
			|| item == ModItems.PRINTERS_INSTRUCTION_SHEET
			|| item == ModItems.WORKSHOP_MAP_FRAGMENT
			|| item == ModItems.DECORATIVE_WOODCUT
			|| item == ModItems.PRINTED_WARNING_POSTER
			|| item == ModItems.RESTORED_CHRONICLE_PAGE
			|| item == ModItems.VILLAGE_CHRONICLE_PRINT
			|| item == ModItems.FORBIDDEN_NOTICE_PRINT
			|| item == ModItems.PRESS_SCREW
			|| item == ModItems.PRESS_HANDLE
			|| item == ModItems.PRESS_PLATEN
			|| item == ModItems.PRESS_CARRIAGE;
	}

	// ── Container ──────────────────────────────────────────────────────────

	@Override
	public int getContainerSize() {
		return DRAWER_COUNT;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : drawers) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return drawers.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack result = ContainerHelper.removeItem(drawers, slot, amount);
		if (!result.isEmpty()) {
			setChanged();
		}
		return result;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(drawers, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		drawers.set(slot, stack);
		stack.limitSize(getMaxStackSize(stack));
		setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		drawers.clear();
	}

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

	@Override
	protected void saveAdditional(ValueOutput tag) {
		super.saveAdditional(tag);
		ContainerHelper.saveAllItems(tag, drawers);
		tag.putFloat("OpenAnim", openAnim);
		tag.putInt("TargetOpenDrawer", targetOpenDrawer);
	}

	@Override
	protected void loadAdditional(ValueInput tag) {
		super.loadAdditional(tag);
		drawers.clear();
		ContainerHelper.loadAllItems(tag, drawers);
		openAnim = tag.getFloatOr("OpenAnim", 0.0F);
		targetOpenDrawer = tag.getIntOr("TargetOpenDrawer", -1);
	}
}
