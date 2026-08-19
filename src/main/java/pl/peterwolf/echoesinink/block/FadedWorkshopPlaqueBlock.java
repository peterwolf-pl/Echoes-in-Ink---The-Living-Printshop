package pl.peterwolf.echoesinink.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Thin wall-mounted maker plaque with a clear investigation instruction. */
public final class FadedWorkshopPlaqueBlock extends InvestigatableBlock {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final MapCodec<FadedWorkshopPlaqueBlock> CODEC = simpleCodec(FadedWorkshopPlaqueBlock::new);

	private static final VoxelShape NORTH = Block.box(2, 2, 14, 14, 14, 16);
	private static final VoxelShape SOUTH = Block.box(2, 2, 0, 14, 14, 2);
	private static final VoxelShape WEST = Block.box(14, 2, 2, 16, 14, 14);
	private static final VoxelShape EAST = Block.box(0, 2, 2, 2, 14, 14);

	public FadedWorkshopPlaqueBlock(Properties properties) {
		super(properties, InvestigationLoot.Profile.PLAQUE);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<FadedWorkshopPlaqueBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
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

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction face = context.getClickedFace();
		if (face.getAxis().isVertical()) {
			face = context.getHorizontalDirection().getOpposite();
		}
		return defaultBlockState().setValue(FACING, face);
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
			serverPlayer.sendSystemMessage(
				Component.translatable("block.echoes_in_ink.faded_workshop_plaque.action"),
				true
			);
		}
		return InteractionResult.SUCCESS_SERVER;
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
