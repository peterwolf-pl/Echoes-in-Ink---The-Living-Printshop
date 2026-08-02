package pl.peterwolf.echoesinink.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Non-investigatable floorboards. The legacy investigation property remains in
 * the state definition so existing world palettes load without an unknown-state
 * migration, but the Printer's Brush no longer treats this block as a target.
 */
public final class DecorativeFloorboardsBlock extends Block {
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
}
