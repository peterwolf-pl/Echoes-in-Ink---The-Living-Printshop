package pl.peterwolf.echoesinink.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Test / workshop debris that can be brushed clean.
 * Investigation loot arrives fully in Phase 2; Phase 1 only advances visual state.
 */
public class PrintingDebrisBlock extends Block implements Investigatable {
	public static final MapCodec<PrintingDebrisBlock> CODEC = simpleCodec(PrintingDebrisBlock::new);
	public static final EnumProperty<InvestigationState> INVESTIGATION =
		EnumProperty.create("investigation", InvestigationState.class);

	public PrintingDebrisBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(INVESTIGATION, InvestigationState.UNTOUCHED));
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(INVESTIGATION);
	}

	@Override
	public boolean canClean(BlockState state) {
		return state.getValue(INVESTIGATION).canClean();
	}

	@Override
	public boolean clean(ServerLevel level, BlockPos pos, BlockState state, Player player) {
		InvestigationState current = state.getValue(INVESTIGATION);
		if (!current.canClean()) {
			return false;
		}
		InvestigationState next = current.next();
		level.setBlock(pos, state.setValue(INVESTIGATION, next), Block.UPDATE_ALL);
		return true;
	}
}
