package pl.peterwolf.echoesinink.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import pl.peterwolf.echoesinink.block.HangingPosterBlock;
import pl.peterwolf.echoesinink.block.LaidPaperBlock;
import pl.peterwolf.echoesinink.block.ModBlocks;
import pl.peterwolf.echoesinink.block.PosterKind;
import pl.peterwolf.echoesinink.sound.ModSounds;

/**
 * Shared “lay this item on a flat top surface” interaction.
 * Used for pages, prints, matrices, and press parts (stored in {@link LaidPaperBlock}).
 */
public final class PrintPlacement {
	private PrintPlacement() {}

	/** Hang a readable print on a solid vertical face. */
	public static InteractionResult tryHangOnWall(UseOnContext context) {
		Direction face = context.getClickedFace();
		if (face.getAxis().isVertical()) {
			return InteractionResult.PASS;
		}

		Level level = context.getLevel();
		BlockPos placePos = context.getClickedPos().relative(face);
		Player player = context.getPlayer();
		ItemStack stack = context.getItemInHand();

		if (!level.getBlockState(placePos).canBeReplaced()) {
			return InteractionResult.FAIL;
		}
		if (!level.getBlockState(context.getClickedPos()).isFaceSturdy(level, context.getClickedPos(), face)) {
			return InteractionResult.FAIL;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		var state = ModBlocks.HANGING_POSTER.defaultBlockState()
			.setValue(HangingPosterBlock.FACING, face)
			.setValue(HangingPosterBlock.KIND, PosterKind.fromItem(stack.getItem()));
		if (!level.setBlock(placePos, state, 3)) {
			return InteractionResult.FAIL;
		}
		level.playSound(null, placePos, ModSounds.PRESS_LOAD, SoundSource.BLOCKS, 0.6F, 0.9F);
		level.gameEvent(GameEvent.BLOCK_PLACE, placePos, GameEvent.Context.of(player, state));
		if (player == null || !player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendOverlayMessage(Component.translatable("block.echoes_in_ink.hanging_poster.placed"));
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	public static InteractionResult tryLayOnTop(UseOnContext context) {
		if (context.getClickedFace() != Direction.UP) {
			return InteractionResult.PASS;
		}

		Level level = context.getLevel();
		BlockPos placePos = context.getClickedPos().above();
		Player player = context.getPlayer();
		ItemStack stack = context.getItemInHand();

		if (!level.getBlockState(context.getClickedPos()).isFaceSturdy(level, context.getClickedPos(), Direction.UP)) {
			return InteractionResult.FAIL;
		}
		if (!level.getBlockState(placePos).canBeReplaced()) {
			return InteractionResult.FAIL;
		}

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		Direction facing = player != null
			? player.getDirection().getOpposite()
			: context.getHorizontalDirection().getOpposite();

		if (!LaidPaperBlock.placePage(level, placePos, facing, stack, player)) {
			return InteractionResult.FAIL;
		}

		level.playSound(null, placePos, ModSounds.PRESS_COLLECT, SoundSource.BLOCKS, 0.45F, 1.25F);
		level.gameEvent(
			GameEvent.BLOCK_PLACE,
			placePos,
			GameEvent.Context.of(player, ModBlocks.LAID_PAPER.defaultBlockState())
		);
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendOverlayMessage(Component.translatable("block.echoes_in_ink.laid_paper.placed"));
		}
		return InteractionResult.SUCCESS_SERVER;
	}
}
