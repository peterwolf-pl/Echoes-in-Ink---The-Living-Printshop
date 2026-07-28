package pl.peterwolf.echoesinink.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.block.entity.InvestigationBlockEntity;

/**
 * Workshop block with persistent investigation state and once-only loot.
 */
public class InvestigatableBlock extends BaseEntityBlock implements Investigatable {
	public static final EnumProperty<InvestigationState> INVESTIGATION =
		EnumProperty.create("investigation", InvestigationState.class);

	private final InvestigationLoot.Profile lootProfile;
	private final MapCodec<InvestigatableBlock> codec;

	public InvestigatableBlock(Properties properties, InvestigationLoot.Profile lootProfile) {
		super(properties);
		this.lootProfile = lootProfile;
		this.codec = simpleCodec(props -> new InvestigatableBlock(props, lootProfile));
		registerDefaultState(stateDefinition.any().setValue(INVESTIGATION, InvestigationState.UNTOUCHED));
	}

	public InvestigationLoot.Profile lootProfile() {
		return lootProfile;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return codec;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(INVESTIGATION);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new InvestigationBlockEntity(pos, state);
	}

	@Override
	public boolean canClean(BlockState state) {
		return state.getValue(INVESTIGATION).canClean();
	}

	@Override
	public boolean clean(ServerLevel level, BlockPos pos, BlockState state, Player player) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof InvestigationBlockEntity investigation) {
			return investigation.clean(level, player);
		}
		if (!canClean(state)) {
			return false;
		}
		level.setBlock(pos, state.setValue(INVESTIGATION, state.getValue(INVESTIGATION).next()), Block.UPDATE_ALL);
		return true;
	}
}
