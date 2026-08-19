package pl.peterwolf.echoesinink.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import java.util.List;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.item.ModItems;

/**
 * Thin paper poster hung on a wall. {@link #KIND} selects the printed design.
 * FACING = direction the printed face points (away from the wall).
 */
public class HangingPosterBlock extends HorizontalDirectionalBlock {
	public static final MapCodec<HangingPosterBlock> CODEC = simpleCodec(HangingPosterBlock::new);
	public static final EnumProperty<PosterKind> KIND = EnumProperty.create("kind", PosterKind.class);

	private static final VoxelShape NORTH = Block.box(1, 1, 15, 15, 15, 16);
	private static final VoxelShape SOUTH = Block.box(1, 1, 0, 15, 15, 1);
	private static final VoxelShape WEST = Block.box(15, 1, 1, 16, 15, 15);
	private static final VoxelShape EAST = Block.box(0, 1, 1, 1, 15, 15);

	public HangingPosterBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(KIND, PosterKind.WARNING));
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, KIND);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case SOUTH -> SOUTH;
			case WEST -> WEST;
			case EAST -> EAST;
			default -> NORTH;
		};
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction face = context.getClickedFace();
		if (face.getAxis().isVertical()) {
			return null;
		}
		return defaultBlockState().setValue(FACING, face);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction face = state.getValue(FACING);
		BlockPos support = pos.relative(face.getOpposite());
		return level.getBlockState(support).isFaceSturdy(level, support, face);
	}

	@Override
	protected BlockState updateShape(
		BlockState state,
		LevelReader level,
		ScheduledTickAccess ticks,
		BlockPos pos,
		Direction direction,
		BlockPos neighborPos,
		BlockState neighborState,
		net.minecraft.util.RandomSource random
	) {
		if (!canSurvive(state, level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
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
		if (stack.is(ModItems.MAGNIFYING_LENS)) {
			return InteractionResult.PASS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}

	@Override
	protected InteractionResult useWithoutItem(
		BlockState state,
		Level level,
		BlockPos pos,
		Player player,
		BlockHitResult hit
	) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			state.getValue(KIND).showPrint(serverPlayer);
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		return List.of(new ItemStack(state.getValue(KIND).dropItem()));
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(state.getValue(KIND).dropItem());
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}
