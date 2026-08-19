package pl.peterwolf.echoesinink.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.block.entity.ModBlockEntities;
import pl.peterwolf.echoesinink.block.entity.PrintingPressBlockEntity;

/**
 * Historical screw printing press — physical world interaction (not a furnace GUI).
 */
public class PrintingPressBlock extends BaseEntityBlock {
	public static final MapCodec<PrintingPressBlock> CODEC = simpleCodec(PrintingPressBlock::new);
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<PressPhase> PHASE = EnumProperty.create("phase", PressPhase.class,
		PressPhase.INCOMPLETE, PressPhase.IDLE, PressPhase.CARRIAGE_IN, PressPhase.PRESSING,
		PressPhase.OUTPUT_READY, PressPhase.JAMMED);

	/** Standing hand-press silhouette: four legs, stretchers, raised bed, cheeks, and crosshead. */
	private static final VoxelShape SHAPE = Shapes.or(
		Block.box(1, 0, 1, 4, 14, 4),
		Block.box(12, 0, 1, 15, 14, 4),
		Block.box(1, 0, 12, 4, 14, 15),
		Block.box(12, 0, 12, 15, 14, 15),
		Block.box(1.5, 5, 3.5, 3.5, 7, 12.5),
		Block.box(12.5, 5, 3.5, 14.5, 7, 12.5),
		Block.box(3.5, 5, 1.5, 12.5, 7, 3.5),
		Block.box(3.5, 5, 12.5, 12.5, 7, 14.5),
		Block.box(0, 12, 0, 4, 16, 16),
		Block.box(12, 12, 0, 16, 16, 16),
		Block.box(1, 14, 4, 4, 28, 12),
		Block.box(12, 14, 4, 15, 28, 12),
		Block.box(0, 25, 3, 16, 28, 13),
		Block.box(3, 14, 0, 13, 18, 16)
	);

	public PrintingPressBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(PHASE, PressPhase.INCOMPLETE));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, PHASE);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		// Draw block model + BER animation layers.
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PrintingPressBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide()
			? null
			: createTickerHelper(type, ModBlockEntities.PRINTING_PRESS, PrintingPressBlockEntity::serverTick);
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
		if (!(level.getBlockEntity(pos) instanceof PrintingPressBlockEntity press)) {
			if (!level.isClientSide()) {
				hint(player, Component.translatable("press.echoes_in_ink.no_entity"));
			}
			return InteractionResult.SUCCESS;
		}

		// Client: only swing hand; server owns logic.
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		// 1) Install machine parts
		if (press.tryInstallPart(player, stack)) {
			hint(player, Component.translatable("press.echoes_in_ink.part_installed"));
			hint(player, press.nextStepMessage());
			return InteractionResult.SUCCESS;
		}

		// 2) Insert matrix / ink / paper
		if (press.tryInsertInput(player, stack)) {
			hint(player, Component.translatable("press.echoes_in_ink.input_inserted"));
			hint(player, press.nextStepMessage());
			return InteractionResult.SUCCESS;
		}

		// 3) Held item not used as input — empty-hand sequence only when stack empty.
		if (!stack.isEmpty()) {
			if (player.isSecondaryUseActive()) {
				hint(player, press.statusMessage());
				hint(player, press.nextStepMessage());
			} else {
				hint(player, press.nextStepMessage());
			}
			return InteractionResult.SUCCESS;
		}

		return emptyHandInteract(press, player);
	}

	@Override
	protected InteractionResult useWithoutItem(
		BlockState state,
		Level level,
		BlockPos pos,
		Player player,
		BlockHitResult hit
	) {
		if (!(level.getBlockEntity(pos) instanceof PrintingPressBlockEntity press)) {
			if (!level.isClientSide()) {
				hint(player, Component.translatable("press.echoes_in_ink.no_entity"));
			}
			return InteractionResult.SUCCESS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		return emptyHandInteract(press, player);
	}

	/**
	 * Empty hand: run press sequence / drawer eject.
	 * Sneak while idle opens the drawer (remove matrix/ink/paper).
	 * Sneak during other phases only shows status.
	 */
	private static InteractionResult emptyHandInteract(PrintingPressBlockEntity press, Player player) {
		if (player.isSecondaryUseActive() && press.phase() != PressPhase.IDLE && press.phase() != PressPhase.JAMMED) {
			hint(player, press.statusMessage());
			hint(player, press.nextStepMessage());
			return InteractionResult.SUCCESS;
		}
		String key = press.interactEmptyHand(player);
		if (key != null && !key.isEmpty()) {
			hint(player, Component.translatable(key));
		}
		hint(player, press.nextStepMessage());
		return InteractionResult.SUCCESS;
	}

	/** Action-bar feedback so the player always sees the next step. */
	static void hint(Player player, Component text) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendOverlayMessage(text);
		} else {
			player.sendSystemMessage(text);
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PrintingPressBlockEntity press) {
			press.dropAll(level, pos, player);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public void destroy(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
		if (level instanceof Level realLevel && realLevel.getBlockEntity(pos) instanceof PrintingPressBlockEntity press) {
			press.dropAll(realLevel, pos);
		}
		super.destroy(level, pos, state);
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
