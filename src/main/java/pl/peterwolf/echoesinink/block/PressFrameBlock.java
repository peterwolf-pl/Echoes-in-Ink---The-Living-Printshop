package pl.peterwolf.echoesinink.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pl.peterwolf.echoesinink.block.entity.PrintingPressBlockEntity;

/**
 * Investigation wreck of the Historical Screw Printing Press — same silhouette
 * as {@link PrintingPressBlock} (incomplete frame), not a solid cube.
 */
public class PressFrameBlock extends InvestigatableBlock {
	/** Same standing press silhouette as {@link PrintingPressBlock}. */
	private static final VoxelShape SHAPE = Shapes.or(
		Block.box(1, 0, 1, 4, 14, 4),
		Block.box(12, 0, 1, 15, 14, 4),
		Block.box(1, 0, 12, 4, 14, 15),
		Block.box(12, 0, 12, 15, 14, 15),
		Block.box(1.5, 5, 3.5, 3.5, 7, 12.5),
		Block.box(12.5, 5, 3.5, 14.5, 7, 12.5),
		Block.box(3.5, 5, 1.5, 12.5, 7, 3.5),
		Block.box(3.5, 5, 12.5, 12.5, 7, 14.5),
		Block.box(0, 12, 0, 4, 16, 16),
		Block.box(12, 12, 0, 16, 16, 16),
		Block.box(1, 14, 4, 4, 28, 12),
		Block.box(12, 14, 4, 15, 28, 12),
		Block.box(0, 25, 3, 16, 28, 13),
		Block.box(3, 14, 0, 13, 18, 16)
	);

	public PressFrameBlock(Properties properties) {
		super(properties, InvestigationLoot.Profile.PRESS);
	}

	@Override
	protected InteractionResult useItemOn(
		ItemStack stack,
		BlockState state,
		Level level,
		BlockPos pos,
		Player player,
		InteractionHand hand,
		BlockHitResult hit
	) {
		if (!PrintingPressBlockEntity.isPressPart(stack)) {
			return super.useItemOn(stack, state, level, pos, player, hand, hit);
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		return convertAndInstall(level, pos, player, stack)
			? InteractionResult.SUCCESS_SERVER
			: InteractionResult.FAIL;
	}

	/**
	 * Turns a recovered workshop frame into the functional press controller and
	 * installs the first component. Natural printshops contain the frame, so this
	 * is the normal survival assembly path rather than a command-only shortcut.
	 */
	static boolean convertAndInstall(Level level, BlockPos pos, Player player, ItemStack stack) {
		if (level.isClientSide() || !PrintingPressBlockEntity.isPressPart(stack)) {
			return false;
		}

		BlockState state = level.getBlockState(pos);
		if (state.is(ModBlocks.PRESS_FRAME)) {
			Direction facing = player.getDirection().getOpposite();
			BlockState pressState = ModBlocks.PRINTING_PRESS.defaultBlockState()
				.setValue(PrintingPressBlock.FACING, facing);
			if (!level.setBlock(pos, pressState, Block.UPDATE_ALL)) {
				return false;
			}
		}

		if (!(level.getBlockEntity(pos) instanceof PrintingPressBlockEntity press)
			|| !press.tryInstallPart(player, stack)) {
			return false;
		}
		PrintingPressBlock.hint(player, Component.translatable("press.echoes_in_ink.part_installed"));
		PrintingPressBlock.hint(player, press.nextStepMessage());
		return true;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state) {
		return SHAPE;
	}
}
