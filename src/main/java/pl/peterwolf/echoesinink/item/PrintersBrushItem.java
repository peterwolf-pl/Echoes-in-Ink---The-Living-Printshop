package pl.peterwolf.echoesinink.item;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import pl.peterwolf.echoesinink.block.FadedWorkshopPlaqueBlock;
import pl.peterwolf.echoesinink.block.Investigatable;
import pl.peterwolf.echoesinink.block.entity.InvestigationBlockEntity;
import pl.peterwolf.echoesinink.config.ModConfig;

/**
 * Cleans historical debris with a timed use action (server-authoritative).
 */
public class PrintersBrushItem extends Item {
	private static final Map<UUID, CleaningTarget> CLEANING_TARGETS = new ConcurrentHashMap<>();

	public PrintersBrushItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player != null) {
			Level level = context.getLevel();
			BlockPos pos = context.getClickedPos();
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() instanceof Investigatable investigatable && investigatable.canClean(state)) {
				// Plaque: refuse to start brush hold until lens-inspected.
				if (!level.isClientSide()
					&& state.getBlock() instanceof FadedWorkshopPlaqueBlock
					&& level.getBlockEntity(pos) instanceof InvestigationBlockEntity inv
					&& !inv.isLensInspected()) {
					if (player instanceof ServerPlayer serverPlayer) {
						serverPlayer.sendSystemMessage(
							Component.translatable("investigation.echoes_in_ink.need_lens_first"),
							true
						);
					}
					return InteractionResult.FAIL;
				}
				CLEANING_TARGETS.put(player.getUUID(), new CleaningTarget(level.dimension(), pos.immutable()));
				player.startUsingItem(context.getHand());
				return InteractionResult.CONSUME;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BRUSH;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return Math.max(10, ModConfig.INSTANCE.brushCleaningDurationTicks + 5);
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
		if (!(entity instanceof Player player)) {
			entity.releaseUsingItem();
			return;
		}

		CleaningTarget target = CLEANING_TARGETS.get(player.getUUID());
		if (target == null
			|| !target.dimension().equals(level.dimension())
			|| !level.isLoaded(target.pos())
			|| player.distanceToSqr(
				target.pos().getX() + 0.5,
				target.pos().getY() + 0.5,
				target.pos().getZ() + 0.5
			) > 36.0D) {
			stopCleaning(entity);
			return;
		}
		BlockPos pos = target.pos();
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof Investigatable investigatable) || !investigatable.canClean(state)) {
			stopCleaning(entity);
			return;
		}

		int used = getUseDuration(stack, entity) - remainingUseDuration + 1;
		int required = Math.max(1, ModConfig.INSTANCE.brushCleaningDurationTicks);

		// Periodic feedback every 5 ticks
		if (used % 5 == 0) {
			level.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 0.4F, 1.0F);
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(
					new BlockParticleOption(ParticleTypes.BLOCK, state),
					pos.getX() + 0.5,
					pos.getY() + 1.0,
					pos.getZ() + 0.5,
					4,
					0.2, 0.1, 0.2,
					0.02
				);
			}
		}

		if (used >= required && !level.isClientSide() && level instanceof ServerLevel serverLevel) {
			boolean cleaned = investigatable.clean(serverLevel, pos, state, player);
			if (cleaned) {
				serverLevel.playSound(null, pos, SoundEvents.BRUSH_SAND_COMPLETED, SoundSource.BLOCKS, 0.8F, 1.0F);
				int cost = Math.max(0, ModConfig.INSTANCE.brushDurabilityCost);
				if (cost > 0 && !player.getAbilities().instabuild) {
					stack.hurtAndBreak(cost, player, player.getUsedItemHand() == InteractionHand.MAIN_HAND
						? EquipmentSlot.MAINHAND
						: EquipmentSlot.OFFHAND);
				}
				if (player instanceof ServerPlayer serverPlayer) {
					serverPlayer.sendSystemMessage(Component.translatable("item.echoes_in_ink.printers_brush.cleaned"), true);
				}
			}
			stopCleaning(entity);
		}
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
		if (entity instanceof Player player) {
			CLEANING_TARGETS.remove(player.getUUID());
		}
		return super.releaseUsing(stack, level, entity, timeLeft);
	}

	private static void stopCleaning(LivingEntity entity) {
		if (entity instanceof Player player) {
			CLEANING_TARGETS.remove(player.getUUID());
		}
		entity.releaseUsingItem();
	}

	private record CleaningTarget(ResourceKey<Level> dimension, BlockPos pos) {}

	@Override
	public void appendHoverText(
		ItemStack stack,
		TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> tooltip,
		TooltipFlag flag
	) {
		tooltip.accept(Component.translatable("item.echoes_in_ink.printers_brush.desc").withColor(0xAAAAAA));
	}
}
