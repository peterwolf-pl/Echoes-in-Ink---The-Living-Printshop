package pl.peterwolf.echoesinink.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Empty wooden screw-press frame (cheeks, head, bed rails) awaiting further
 * assembly — not a solid cube.
 */
public class PressFrameBlock extends InvestigatableBlock {
	/** Approximate hollow frame matching the multi-element block models. */
	private static final VoxelShape SHAPE = Shapes.or(
		// corner posts
		Block.box(1, 0, 1, 4, 10, 4),
		Block.box(12, 0, 1, 15, 10, 4),
		Block.box(1, 0, 12, 4, 10, 15),
		Block.box(12, 0, 12, 15, 10, 15),
		// bed sill + rails
		Block.box(3, 6, 1, 13, 8, 15),
		// cheeks
		Block.box(1, 7, 4.5, 4, 14, 11.5),
		Block.box(12, 7, 4.5, 15, 14, 11.5),
		// head beam + nut
		Block.box(0.5, 12.5, 3.5, 15.5, 15.5, 12.5)
	);

	public PressFrameBlock(Properties properties) {
		super(properties, InvestigationLoot.Profile.PRESS);
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
