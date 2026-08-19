package pl.peterwolf.echoesinink.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Decorative floorboards. Their three visual cleanup states remain sweepable,
 * but cleaning them never creates investigation loot.
 */
public final class DecorativeFloorboardsBlock extends Block implements Investigatable {
	public static final MapCodec<DecorativeFloorboardsBlock> CODEC = simpleCodec(DecorativeFloorboardsBlock::new);

	public DecorativeFloorboardsBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(
			InvestigatableBlock.INVESTIGATION,
			InvestigationState.UNTOUCHED
		));
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(InvestigatableBlock.INVESTIGATION);
	}

	@Override
	public boolean canClean(BlockState state) {
		return state.getValue(InvestigatableBlock.INVESTIGATION).canClean();
	}

	@Override
	public boolean clean(ServerLevel level, BlockPos pos, BlockState state, Player player) {
		if (!canClean(state)) {
			return false;
		}
		level.setBlock(
			pos,
			state.setValue(
				InvestigatableBlock.INVESTIGATION,
				state.getValue(InvestigatableBlock.INVESTIGATION).next()
			),
			Block.UPDATE_ALL
		);
		return true;
	}
}
