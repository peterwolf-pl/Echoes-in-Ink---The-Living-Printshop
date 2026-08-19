package pl.peterwolf.echoesinink.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Workshop composing table — legs + top + lower shelf (not a solid cube).
 */
public class DustyPrintingTableBlock extends InvestigatableBlock {
	private static final VoxelShape SHAPE = Shapes.or(
		// tabletop
		Block.box(0, 13, 0, 16, 16, 16),
		// apron
		Block.box(1, 11, 1, 15, 13, 15),
		// legs
		Block.box(1, 0, 1, 3, 13, 3),
		Block.box(13, 0, 1, 15, 13, 3),
		Block.box(1, 0, 13, 3, 13, 15),
		Block.box(13, 0, 13, 15, 13, 15),
		// lower shelf
		Block.box(3, 4, 3, 13, 5, 13)
	);

	public DustyPrintingTableBlock(Properties properties) {
		super(properties, InvestigationLoot.Profile.TABLE);
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
