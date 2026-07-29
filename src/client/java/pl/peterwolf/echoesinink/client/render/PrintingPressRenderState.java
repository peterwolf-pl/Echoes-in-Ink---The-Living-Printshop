package pl.peterwolf.echoesinink.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.block.PressPhase;

public class PrintingPressRenderState extends BlockEntityRenderState {
	public Direction facing = Direction.NORTH;
	public PressPhase phase = PressPhase.INCOMPLETE;
	public float animProgress;
	public boolean hasScrew;
	public boolean hasHandle;
	public boolean hasPlaten;
	public boolean hasCarriage;
	public ItemStack output = ItemStack.EMPTY;
	@Nullable public ItemStackRenderState matrixRenderState;
	@Nullable public ItemStackRenderState inkRenderState;
	@Nullable public ItemStackRenderState sheetRenderState;
	@Nullable public ItemStackRenderState screwRenderState;
	@Nullable public ItemStackRenderState handleRenderState;
	@Nullable public ItemStackRenderState platenRenderState;
	@Nullable public ItemStackRenderState carriageRenderState;
}
