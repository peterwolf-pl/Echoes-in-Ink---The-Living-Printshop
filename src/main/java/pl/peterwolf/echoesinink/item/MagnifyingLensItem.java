package pl.peterwolf.echoesinink.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import pl.peterwolf.echoesinink.block.ModBlocks;
import pl.peterwolf.echoesinink.config.ModConfig;

/**
 * Magnifying lens: hold right-click to look through (spyglass-style zoom);
 * right-click a block to inspect recoverable details (server).
 */
public class MagnifyingLensItem extends Item {
	private static final Map<UUID, Long> LAST_INSPECT_TICK = new HashMap<>();

	public MagnifyingLensItem(Properties properties) {
		super(properties);
	}

	/** Hold to look through the glass (client FOV via isScoping mixin). */
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		player.startUsingItem(hand);
		if (!level.isClientSide()) {
			level.playSound(null, player.blockPosition(), SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 0.5F, 1.15F);
		}
		return InteractionResult.CONSUME;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.SPYGLASS;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		// Long hold like spyglass; release stops zoom.
		return 1200;
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
		if (!level.isClientSide() && entity instanceof Player player) {
			level.playSound(null, player.blockPosition(), SoundEvents.SPYGLASS_STOP_USING, SoundSource.PLAYERS, 0.5F, 1.15F);
		}
		return super.releaseUsing(stack, level, entity, timeLeft);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}

		// Sneak + block: inspect. Plain block click still inspects (historical tools).
		// Looking through is use() when not targeting a usable block inspect path —
		// we inspect first, then player can use in air to zoom.
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		long now = level.getGameTime();
		int cooldown = Math.max(0, ModConfig.INSTANCE.lensInspectCooldownTicks);
		Long last = LAST_INSPECT_TICK.get(serverPlayer.getUUID());
		if (last != null && now - last < cooldown) {
			return InteractionResult.FAIL;
		}
		LAST_INSPECT_TICK.put(serverPlayer.getUUID(), now);

		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		Component result = inspect(state);
		serverPlayer.sendSystemMessage(result, true);
		level.playSound(null, pos, SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 0.35F, 1.4F);
		return InteractionResult.SUCCESS_SERVER;
	}

	/**
	 * Server-side inspection table. At least three distinct findings for Phase 1 tests.
	 */
	static Component inspect(BlockState state) {
		Block block = state.getBlock();
		if (block == ModBlocks.PRINTING_DEBRIS) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.debris");
		}
		if (block == ModBlocks.CARVED_WOODEN_MATRIX) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.matrix");
		}
		if (block == Blocks.BOOKSHELF || block == ModBlocks.DAMAGED_ARCHIVE_SHELF) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.shelf");
		}
		if (block == Blocks.CRAFTING_TABLE || block == ModBlocks.DUSTY_PRINTING_TABLE) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.table");
		}
		if (block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL
			|| block == ModBlocks.BROKEN_PRESS_FRAME) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.press");
		}
		if (block == ModBlocks.COLLAPSED_TYPE_CABINET) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.debris");
		}
		if (block == ModBlocks.FADED_WORKSHOP_PLAQUE) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.shelf");
		}
		if (block == ModBlocks.LAID_PAPER) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.paper");
		}
		if (block == ModBlocks.HANGING_POSTER) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.poster");
		}
		return Component.translatable("item.echoes_in_ink.magnifying_lens.find.nothing");
	}

	@Override
	public void appendHoverText(
		ItemStack stack,
		TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> tooltip,
		TooltipFlag flag
	) {
		tooltip.accept(Component.translatable("item.echoes_in_ink.magnifying_lens.desc").withColor(0xAAAAAA));
		tooltip.accept(Component.translatable("item.echoes_in_ink.magnifying_lens.look_hint").withColor(0xC0C0FF));
		tooltip.accept(Component.translatable("item.echoes_in_ink.magnifying_lens.inspect_hint").withColor(0xC0C0FF));
	}
}
