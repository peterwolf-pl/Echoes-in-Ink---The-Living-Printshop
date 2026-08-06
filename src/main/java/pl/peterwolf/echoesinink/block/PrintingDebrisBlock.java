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
	private static final VoxelShape SHAPE = Shapes.or(
		Block.box(1, 0, 2, 7, 3, 9),
		Block.box(6, 0, 5, 14, 2, 14),
		Block.box(2, 2, 3, 11, 5, 8),
		Block.box(9, 0, 1, 15, 4, 6),
		Block.box(3, 4, 6, 8, 7, 12),
		Block.box(10, 3, 8, 13, 6, 15),
		Block.box(1, 0, 11, 5, 2, 15)
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
