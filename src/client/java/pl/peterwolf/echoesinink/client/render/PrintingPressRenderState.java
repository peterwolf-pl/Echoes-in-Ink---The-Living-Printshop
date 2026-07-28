package pl.peterwolf.echoesinink.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import pl.peterwolf.echoesinink.block.PressPhase;

public class PrintingPressRenderState extends BlockEntityRenderState {
	public Direction facing = Direction.NORTH;
	public PressPhase phase = PressPhase.INCOMPLETE;
	public float animProgress;
	public boolean hasScrew;
	public boolean hasHandle;
	public boolean hasPlaten;
	public boolean hasCarriage;
	public ItemStack matrix = ItemStack.EMPTY;
	public ItemStack paper = ItemStack.EMPTY;
	public ItemStack output = ItemStack.EMPTY;
}
