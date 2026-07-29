package pl.peterwolf.echoesinink.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import pl.peterwolf.echoesinink.block.HangingPosterBlock;
import pl.peterwolf.echoesinink.block.ModBlocks;
import pl.peterwolf.echoesinink.sound.ModSounds;

/**
 * Readable poster that can also be hung on a solid wall face.
 */
public class PrintedWarningPosterItem extends ReadablePrintItem {
	public PrintedWarningPosterItem(Properties properties) {
		super(properties, "printed_warning_poster", 5);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		Direction face = context.getClickedFace();
		// Top of a block → lay the print flat (same look as on the press).
		if (face == Direction.UP) {
			return pl.peterwolf.echoesinink.util.PrintPlacement.tryLayOnTop(context);
		}
		if (face.getAxis().isVertical()) {
			return InteractionResult.PASS;
		}

		BlockPos placePos = context.getClickedPos().relative(face);
		Player player = context.getPlayer();
		if (!level.getBlockState(placePos).canBeReplaced()) {
			return InteractionResult.FAIL;
		}
		// Need solid support behind the poster.
		if (!level.getBlockState(context.getClickedPos()).isFaceSturdy(level, context.getClickedPos(), face)) {
			return InteractionResult.FAIL;
		}

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		BlockState state = ModBlocks.HANGING_POSTER.defaultBlockState()
			.setValue(HangingPosterBlock.FACING, face);
		if (!level.setBlock(placePos, state, 3)) {
			return InteractionResult.FAIL;
		}
		level.playSound(null, placePos, ModSounds.PRESS_LOAD, SoundSource.BLOCKS, 0.6F, 0.9F);
		level.gameEvent(GameEvent.BLOCK_PLACE, placePos, GameEvent.Context.of(player, state));

		ItemStack stack = context.getItemInHand();
		if (player == null || !player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendOverlayMessage(
				net.minecraft.network.chat.Component.translatable("block.echoes_in_ink.hanging_poster.placed")
			);
		}
		return InteractionResult.SUCCESS_SERVER;
	}
}
