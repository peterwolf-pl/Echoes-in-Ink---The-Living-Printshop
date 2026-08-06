package pl.peterwolf.echoesinink.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TypeCabinetRenderState extends BlockEntityRenderState {
	public Direction facing = Direction.NORTH;
	public int openDrawer = -1;
	public float openAnim;
	public ItemStack[] drawerStacks = new ItemStack[4];
	@Nullable public ItemStackRenderState[] drawerItemStates = new ItemStackRenderState[4];
}
