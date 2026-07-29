package pl.peterwolf.echoesinink.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class LaidPaperRenderState extends BlockEntityRenderState {
	public Direction facing = Direction.NORTH;
	public ItemStack page = ItemStack.EMPTY;
	@Nullable public ItemStackRenderState pageRenderState;
}
