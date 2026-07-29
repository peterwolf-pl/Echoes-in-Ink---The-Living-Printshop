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
import pl.peterwolf.echoesinink.block.LaidPaperBlock;
import pl.peterwolf.echoesinink.block.ModBlocks;
import pl.peterwolf.echoesinink.sound.ModSounds;

/** Shared “lay this page on a flat top surface” interaction. */
public final class PrintPlacement {
	private PrintPlacement() {}

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
