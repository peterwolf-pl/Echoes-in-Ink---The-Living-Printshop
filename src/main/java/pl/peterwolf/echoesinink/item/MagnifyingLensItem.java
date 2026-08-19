package pl.peterwolf.echoesinink.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import pl.peterwolf.echoesinink.block.entity.InvestigationBlockEntity;
import pl.peterwolf.echoesinink.block.entity.LaidPaperBlockEntity;
import pl.peterwolf.echoesinink.config.ModConfig;

/**
 * Magnifying lens: hold right-click to look through (spyglass-style zoom);
 * right-click a block to inspect recoverable details (server) and start zoom.
 */
public class MagnifyingLensItem extends Item {
	private static final Map<UUID, Long> LAST_INSPECT_TICK = new HashMap<>();

	public MagnifyingLensItem(Properties properties) {
		super(properties);
	}

	/** Hold to look through the glass (FOV via isScoping + client FOV mixin). */
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
		return 1200;
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
		if (!level.isClientSide() && entity instanceof Player player) {
			level.playSound(null, player.blockPosition(), SoundEvents.SPYGLASS_STOP_USING, SoundSource.PLAYERS, 0.5F, 1.15F);
		}
		return super.releaseUsing(stack, level, entity, timeLeft);
	}

	/**
	 * Inspect the targeted block, then keep holding for zoom.
	 * Must start using the item so scoping FOV works even when looking at a block.
	 */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}

		// Always enter use-hold so zoom works (useOn SUCCESS previously blocked use()).
		player.startUsingItem(context.getHand());

		if (level.isClientSide()) {
			return InteractionResult.CONSUME;
		}

		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.CONSUME;
		}

		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		// Unlock the exact plaque even if the informational message is on cooldown.
		if (level.getBlockEntity(pos) instanceof InvestigationBlockEntity investigation) {
			investigation.markLensInspected(serverPlayer);
		}

		long now = level.getGameTime();
		int cooldown = Math.max(0, ModConfig.INSTANCE.lensInspectCooldownTicks);
		Long last = LAST_INSPECT_TICK.get(serverPlayer.getUUID());
		if (last == null || now - last >= cooldown) {
			LAST_INSPECT_TICK.put(serverPlayer.getUUID(), now);
			Component result = inspectTarget(level, pos, state, serverPlayer);
			serverPlayer.sendSystemMessage(result, true);
			if (state.is(ModBlocks.LOOSE_INK_STAINED_FLOORBOARDS)
				|| state.is(ModBlocks.HIDDEN_FLOOR_COMPARTMENT)) {
				((ServerLevel) level).sendParticles(
					ParticleTypes.ENCHANT,
					pos.getX() + 0.5,
					pos.getY() + 1.03,
					pos.getZ() + 0.5,
					4,
					0.18,
					0.02,
					0.18,
					0.01
				);
				level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.28F, 1.65F);
			} else {
				level.playSound(null, pos, SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 0.35F, 1.4F);
			}
		}
		return InteractionResult.CONSUME;
	}

	private static Component inspectTarget(Level level, BlockPos pos, BlockState state, ServerPlayer player) {
		if (state.is(ModBlocks.LAID_PAPER)
			&& level.getBlockEntity(pos) instanceof LaidPaperBlockEntity laid) {
			return inspectLaidItem(laid.page(), player);
		}
		if (state.is(ModBlocks.HANGING_POSTER)) {
			if (ModItems.PRINTED_WARNING_POSTER instanceof ReadablePrintItem readable) {
				readable.showPrint(player);
			}
			return Component.translatable(
				"item.echoes_in_ink.magnifying_lens.find.poster",
				new ItemStack(ModItems.PRINTED_WARNING_POSTER).getHoverName()
			);
		}
		return inspect(state);
	}

	private static Component inspectLaidItem(ItemStack displayed, ServerPlayer player) {
		if (displayed.isEmpty()) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.paper");
		}
		if (displayed.getItem() instanceof ReadablePrintItem readable) {
			readable.showPrint(player);
			return Component.translatable(
				"item.echoes_in_ink.magnifying_lens.find.print",
				displayed.getHoverName()
			);
		}
		if (displayed.is(ModItems.RESTORED_CHRONICLE_PAGE)) {
			return Component.translatable(
				"item.echoes_in_ink.magnifying_lens.find.chronicle",
				displayed.getHoverName()
			);
		}
		if (isMatrixOrType(displayed)) {
			return Component.translatable(
				"item.echoes_in_ink.magnifying_lens.find.matrix_item",
				displayed.getHoverName()
			);
		}
		if (displayed.is(ModItems.BLANK_ARCHIVE_PAGE) || displayed.is(ModItems.DAMAGED_ARCHIVE_PAGE)) {
			return Component.translatable(
				"item.echoes_in_ink.magnifying_lens.find.page_item",
				displayed.getHoverName()
			);
		}
		if (isPressPart(displayed)) {
			return Component.translatable(
				"item.echoes_in_ink.magnifying_lens.find.press_part",
				displayed.getHoverName()
			);
		}
		return Component.translatable(
			"item.echoes_in_ink.magnifying_lens.find.workshop_item",
			displayed.getHoverName()
		);
	}

	private static boolean isMatrixOrType(ItemStack stack) {
		return stack.is(ModItems.WOODEN_PRINTING_MATRIX)
			|| stack.is(ModItems.METAL_TYPE_PIECE)
			|| stack.is(ModItems.CHARCOAL_RUBBING)
			|| stack.is(ModItems.VILLAGE_CHRONICLE_MATRIX)
			|| stack.is(ModItems.LEAD_TYPE_SET)
			|| stack.is(ModItems.IRON_CHASE)
			|| stack.is(ModItems.MISSING_HEADLINE_TYPE)
			|| stack.is(ModItems.FORBIDDEN_NOTICE_FORME);
	}

	private static boolean isPressPart(ItemStack stack) {
		return stack.is(ModItems.PRESS_SCREW)
			|| stack.is(ModItems.PRESS_HANDLE)
			|| stack.is(ModItems.PRESS_PLATEN)
			|| stack.is(ModItems.PRESS_CARRIAGE);
	}

	static Component inspect(BlockState state) {
		Block block = state.getBlock();
		if (block == ModBlocks.HIDDEN_FLOOR_COMPARTMENT) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.hidden_floor");
		}
		if (block == ModBlocks.LOOSE_INK_STAINED_FLOORBOARDS) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.loose_floor");
		}
		if (block == ModBlocks.INK_STAINED_FLOORBOARDS) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.decorative_floor");
		}
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
			|| block == ModBlocks.PRESS_FRAME) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.press");
		}
		if (block == ModBlocks.COLLAPSED_TYPE_CABINET) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.debris");
		}
		if (block == ModBlocks.FADED_WORKSHOP_PLAQUE) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.plaque");
		}
		if (block == ModBlocks.LAID_PAPER) {
			return Component.translatable("item.echoes_in_ink.magnifying_lens.find.paper");
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
