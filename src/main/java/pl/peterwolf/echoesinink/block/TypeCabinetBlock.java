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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.block.entity.ModBlockEntities;
import pl.peterwolf.echoesinink.block.entity.TypeCabinetBlockEntity;

/**
 * Type cabinet with thin drawers: open/close animation, one stack per drawer.
 * Still investigatable with brush for progression loot.
 */
public class TypeCabinetBlock extends InvestigatableBlock {
	public static final MapCodec<TypeCabinetBlock> CODEC = simpleCodec(TypeCabinetBlock::new);
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	/** 0 = all closed; 1..4 = which drawer is open (top to bottom). */
	public static final IntegerProperty OPEN_DRAWER = IntegerProperty.create("open_drawer", 0, 4);

	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

	public TypeCabinetBlock(Properties properties) {
		super(properties, InvestigationLoot.Profile.CABINET);
		registerDefaultState(defaultBlockState()
			.setValue(FACING, Direction.NORTH)
			.setValue(OPEN_DRAWER, 0));
	}

	@Override
	protected MapCodec<? extends TypeCabinetBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, OPEN_DRAWER);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TypeCabinetBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide()
			? null
			: createTickerHelper(type, ModBlockEntities.TYPE_CABINET, TypeCabinetBlockEntity::serverTick);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
		if (!(level.getBlockEntity(pos) instanceof TypeCabinetBlockEntity cabinet)) {
			return super.useItemOn(stack, state, level, pos, player, hand, hit);
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (stack.isEmpty()) {
			return cabinetEmptyHand(cabinet, state, level, pos, player);
		}
		// Insert into currently open drawer; open first drawer if closed.
		if (state.getValue(OPEN_DRAWER) == 0) {
			cabinet.openDrawer(level, pos, state, 0);
			state = level.getBlockState(pos);
		}
		if (cabinet.tryInsert(player, stack)) {
			hint(player, Component.translatable("block.echoes_in_ink.collapsed_type_cabinet.inserted"));
			return InteractionResult.SUCCESS_SERVER;
		}
		hint(player, Component.translatable("block.echoes_in_ink.collapsed_type_cabinet.cannot_store"));
		return InteractionResult.FAIL;
	}

	@Override
	protected InteractionResult useWithoutItem(
		BlockState state,
		Level level,
		BlockPos pos,
		Player player,
		BlockHitResult hit
	) {
		if (!(level.getBlockEntity(pos) instanceof TypeCabinetBlockEntity cabinet)) {
			return super.useWithoutItem(state, level, pos, player, hit);
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		return cabinetEmptyHand(cabinet, state, level, pos, player);
	}

	private InteractionResult cabinetEmptyHand(
		TypeCabinetBlockEntity cabinet,
		BlockState state,
		Level level,
		BlockPos pos,
		Player player
	) {
		int open = state.getValue(OPEN_DRAWER);
		if (player.isShiftKeyDown()) {
			// Cycle drawers (or close).
			int next = open >= TypeCabinetBlockEntity.DRAWER_COUNT ? 0 : open + 1;
			if (next == 0) {
				cabinet.closeDrawer(level, pos, state);
				hint(player, Component.translatable("block.echoes_in_ink.collapsed_type_cabinet.closed"));
			} else {
				cabinet.openDrawer(level, pos, state, next - 1);
				hint(player, Component.translatable(
					"block.echoes_in_ink.collapsed_type_cabinet.opened",
					next
				));
			}
			return InteractionResult.SUCCESS_SERVER;
		}
		if (open == 0) {
			cabinet.openDrawer(level, pos, state, 0);
			hint(player, Component.translatable("block.echoes_in_ink.collapsed_type_cabinet.opened", 1));
			return InteractionResult.SUCCESS_SERVER;
		}
		// Extract from open drawer.
		if (cabinet.tryExtract(player)) {
			hint(player, Component.translatable("block.echoes_in_ink.collapsed_type_cabinet.taken"));
			return InteractionResult.SUCCESS_SERVER;
		}
		// Empty open drawer — close it.
		cabinet.closeDrawer(level, pos, state);
		hint(player, Component.translatable("block.echoes_in_ink.collapsed_type_cabinet.closed"));
		return InteractionResult.SUCCESS_SERVER;
	}

	private static void hint(Player player, Component text) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendOverlayMessage(text);
		} else {
			player.sendSystemMessage(text);
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TypeCabinetBlockEntity cabinet) {
			cabinet.dropContents(level, pos);
		}
		return super.playerWillDestroy(level, pos, state, player);
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
