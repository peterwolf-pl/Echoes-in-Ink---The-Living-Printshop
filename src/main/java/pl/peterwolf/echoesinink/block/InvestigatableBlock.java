package pl.peterwolf.echoesinink.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.block.entity.InvestigationBlockEntity;
import pl.peterwolf.echoesinink.item.ModDataComponents;

/**
 * Workshop block with persistent investigation state and once-only loot.
 * Break/place copies progress via {@link InvestigationData} on the item stack.
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

	/** Preserve investigation when the block is broken. */
	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		List<ItemStack> drops = super.getDrops(state, builder);
		BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		InvestigationData data;
		if (be instanceof InvestigationBlockEntity investigation) {
			data = investigation.toItemData(state);
		} else {
			data = InvestigationData.of(false, "", state.getValue(INVESTIGATION));
		}
		for (ItemStack stack : drops) {
			if (stack.is(asItem())) {
				stack.set(ModDataComponents.INVESTIGATION, data);
			}
		}
		return drops;
	}

	/** Creative middle-click / pick block keeps progress. */
	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);
		if (level.getBlockEntity(pos) instanceof InvestigationBlockEntity investigation) {
			stack.set(ModDataComponents.INVESTIGATION, investigation.toItemData(state));
		} else {
			stack.set(ModDataComponents.INVESTIGATION, InvestigationData.of(false, "", state.getValue(INVESTIGATION)));
		}
		return stack;
	}

	/** Restore investigation when placing a carried block. */
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level.isClientSide()) {
			return;
		}
		InvestigationData data = stack.get(ModDataComponents.INVESTIGATION);
		if (data == null) {
			return;
		}
		InvestigationState investigation = data.investigationState();
		BlockState restored = state.setValue(INVESTIGATION, investigation);
		if (restored != state) {
			level.setBlock(pos, restored, Block.UPDATE_CLIENTS);
		}
		if (level.getBlockEntity(pos) instanceof InvestigationBlockEntity investigationBe) {
			investigationBe.applyFromItemData(data);
		}
	}
}
