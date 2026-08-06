package pl.peterwolf.echoesinink.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Printing debris — irregular rubble pile (multi-element models), not a full cube.
 */
public class PrintingDebrisBlock extends InvestigatableBlock {
	/** Matches multi-element rubble models (untouched/partial). */
	private static final VoxelShape SHAPE = Shapes.or(
		Block.box(1, 0, 2, 7, 3, 9),
		Block.box(8, 0, 1, 15, 2, 8),
		Block.box(3, 2, 3, 12, 5, 10),
		Block.box(4, 5, 4, 10, 6, 9),
		Block.box(10, 3, 8, 13, 5, 14),
		Block.box(1, 0, 10, 6, 2, 15),
		Block.box(12, 2, 3, 14, 3, 6)
	);

	public PrintingDebrisBlock(Properties properties) {
		super(properties, InvestigationLoot.Profile.DEBRIS);
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
