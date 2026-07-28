package pl.peterwolf.echoesinink.item;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import pl.peterwolf.echoesinink.block.ModBlocks;
import pl.peterwolf.echoesinink.block.CarvedWoodenMatrixBlock;

/**
 * Consumes one blank sheet and produces a rubbing with a safe pattern identifier component.
 */
public class CharcoalRubbingPaperItem extends Item {
	public CharcoalRubbingPaperItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		Identifier pattern = resolvePattern(state);
		if (pattern == null) {
			if (player instanceof ServerPlayer serverPlayer) {
				serverPlayer.sendSystemMessage(Component.translatable("item.echoes_in_ink.charcoal_rubbing_paper.no_pattern"), true);
			}
			return InteractionResult.FAIL;
		}

		ItemStack hand = context.getItemInHand();
		if (!player.getAbilities().instabuild) {
			hand.shrink(1);
		}

		ItemStack rubbing = new ItemStack(ModItems.CHARCOAL_RUBBING);
		rubbing.set(ModDataComponents.RUBBING_PATTERN, pattern);
		if (!player.getInventory().add(rubbing)) {
			player.drop(rubbing, false);
		}

		level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.7F, 0.9F);
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendSystemMessage(
				Component.translatable("item.echoes_in_ink.charcoal_rubbing_paper.success", pattern.toString()),
				true
			);
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	private static Identifier resolvePattern(BlockState state) {
		if (state.getBlock() instanceof CarvedWoodenMatrixBlock carved) {
			return carved.patternId();
		}
		if (state.is(ModBlocks.CARVED_WOODEN_MATRIX)) {
			return CarvedWoodenMatrixBlock.DEFAULT_PATTERN;
		}
		return null;
	}

	@Override
	public void appendHoverText(
		ItemStack stack,
		TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> tooltip,
		TooltipFlag flag
	) {
		tooltip.accept(Component.translatable("item.echoes_in_ink.charcoal_rubbing_paper.desc").withColor(0xAAAAAA));
	}
}
