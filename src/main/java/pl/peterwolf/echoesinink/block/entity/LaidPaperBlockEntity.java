package pl.peterwolf.echoesinink.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import pl.peterwolf.echoesinink.item.ModItems;

/**
 * Stores the exact laid workshop item (page, print, matrix, or press part).
 */
public class LaidPaperBlockEntity extends BlockEntity {
	private ItemStack page = ItemStack.EMPTY;

	public LaidPaperBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.LAID_PAPER, pos, state);
	}

	public ItemStack page() {
		return page;
	}

	public void setPage(ItemStack stack) {
		this.page = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
		setChanged();
		if (level != null && !level.isClientSide()) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, 3);
		}
	}

	public ItemStack asDrop() {
		if (!page.isEmpty()) {
			return page.copy();
		}
		// Should not happen after a normal place; keep a safe blank fallback.
		return new ItemStack(ModItems.BLANK_ARCHIVE_PAGE);
	}

	@Override
	protected void saveAdditional(ValueOutput tag) {
		super.saveAdditional(tag);
		if (!page.isEmpty()) {
			tag.store("Page", ItemStack.CODEC, page);
		}
	}

	@Override
	protected void loadAdditional(ValueInput tag) {
		super.loadAdditional(tag);
		page = tag.read("Page", ItemStack.CODEC).orElse(ItemStack.EMPTY);
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
