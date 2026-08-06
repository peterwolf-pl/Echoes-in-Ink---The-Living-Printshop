package pl.peterwolf.echoesinink.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.block.entity.LaidPaperBlockEntity;
import pl.peterwolf.echoesinink.item.ModItems;
import pl.peterwolf.echoesinink.item.ReadablePrintItem;
import pl.peterwolf.echoesinink.item.RestoredChroniclePageItem;

/**
 * Workshop object laid on a top surface: page, print, matrix, or press part.
 * The block entity stores the exact item; client BER draws it (flat sheet/matrix
 * or small upright prop for machine parts).
 */
public class LaidPaperBlock extends BaseEntityBlock {
	public static final MapCodec<LaidPaperBlock> CODEC = simpleCodec(LaidPaperBlock::new);
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 1, 15);

	public LaidPaperBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		// Model provides a thin sheet; BER draws the same page as on the press.
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LaidPaperBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		if (context.getClickedFace() != Direction.UP) {
			return null;
		}
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
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
		if (!(level.getBlockEntity(pos) instanceof LaidPaperBlockEntity be)) {
			return InteractionResult.PASS;
		}
		ItemStack page = be.page();
		if (player instanceof ServerPlayer serverPlayer) {
			if (page.getItem() instanceof ReadablePrintItem readable) {
				readable.showPrint(serverPlayer);
			} else if (page.getItem() instanceof RestoredChroniclePageItem) {
				// Chronicle still uses progressive use; show a short note when laid.
				serverPlayer.sendOverlayMessage(
					net.minecraft.network.chat.Component.translatable("print.echoes_in_ink.reading")
				);
			} else {
				serverPlayer.sendOverlayMessage(
					net.minecraft.network.chat.Component.translatable("block.echoes_in_ink.laid_paper.blank_hint")
				);
			}
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LaidPaperBlockEntity be) {
			ItemStack drop = be.asDrop();
			if (!player.getAbilities().instabuild) {
				popResource(level, pos, drop);
			}
			be.setPage(ItemStack.EMPTY);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public void destroy(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
		if (level instanceof Level realLevel
			&& !realLevel.isClientSide()
			&& realLevel.getBlockEntity(pos) instanceof LaidPaperBlockEntity be
			&& !be.page().isEmpty()) {
			popResource(realLevel, pos, be.asDrop());
			be.setPage(ItemStack.EMPTY);
		}
		super.destroy(level, pos, state);
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		if (level.getBlockEntity(pos) instanceof LaidPaperBlockEntity be) {
			return be.asDrop();
		}
		return new ItemStack(ModItems.BLANK_ARCHIVE_PAGE);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	/**
	 * Place a laid page of the given stack. Consumes one item unless creative.
	 * @return true if placed
	 */
	public static boolean placePage(
		Level level,
		BlockPos placePos,
		Direction facing,
		ItemStack stack,
		@Nullable Player player
	) {
		if (level.isClientSide() || stack.isEmpty()) {
			return false;
		}
		if (!level.getBlockState(placePos).canBeReplaced()) {
			return false;
		}
		BlockPos below = placePos.below();
		if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
			return false;
		}

		BlockState state = ModBlocks.LAID_PAPER.defaultBlockState().setValue(FACING, facing);
		if (!level.setBlock(placePos, state, 3)) {
			return false;
		}
		if (level.getBlockEntity(placePos) instanceof LaidPaperBlockEntity be) {
			be.setPage(stack);
		}
		if (player == null || !player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		return true;
	}
}
