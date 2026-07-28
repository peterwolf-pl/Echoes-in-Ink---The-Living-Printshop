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

	/** Frame-like hitbox so it does not look like a solid cube. */
	private static final VoxelShape SHAPE = Shapes.or(
		Block.box(0, 0, 0, 16, 4, 16),
		Block.box(1, 4, 1, 15, 12, 15),
		Block.box(0, 12, 0, 16, 14, 16)
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

		// 3) Item not usable here — still advance machine / show help
		if (player.isSecondaryUseActive()) {
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
		if (player.isSecondaryUseActive()) {
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
	private static void hint(Player player, Component text) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendOverlayMessage(text);
		} else {
			player.sendSystemMessage(text);
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PrintingPressBlockEntity press) {
			press.dropAll(level, pos);
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
