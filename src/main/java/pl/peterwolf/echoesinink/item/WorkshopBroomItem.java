package pl.peterwolf.echoesinink.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import pl.peterwolf.echoesinink.block.DecorativeFloorboardsBlock;

/** Optional workshop cleanup tool for decorative cobwebs and stained floorboards. */
public final class WorkshopBroomItem extends Item {
	public WorkshopBroomItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockPos pos = context.getClickedPos();
		BlockState state = context.getLevel().getBlockState(pos);
		Player player = context.getPlayer();
		if (state.is(Blocks.COBWEB)) {
			if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
				return InteractionResult.SUCCESS;
			}
			serverLevel.destroyBlock(pos, true, player);
			serverLevel.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 0.8F, 0.8F);
			damageBroom(context, player);
			return InteractionResult.SUCCESS_SERVER;
		}

		if (state.getBlock() instanceof DecorativeFloorboardsBlock floorboards
			&& floorboards.canClean(state)) {
			if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
				return InteractionResult.SUCCESS;
			}
			if (!floorboards.clean(serverLevel, pos, state, player)) {
				return InteractionResult.FAIL;
			}
			serverLevel.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 0.7F, 0.9F);
			serverLevel.sendParticles(
				new BlockParticleOption(ParticleTypes.BLOCK, state),
				pos.getX() + 0.5,
				pos.getY() + 1.02,
				pos.getZ() + 0.5,
				8,
				0.3, 0.04, 0.3,
				0.02
			);
			if (player instanceof ServerPlayer serverPlayer) {
				serverPlayer.sendSystemMessage(
					Component.translatable("item.echoes_in_ink.workshop_broom.swept_floor"),
					true
				);
			}
			damageBroom(context, player);
			return InteractionResult.SUCCESS_SERVER;
		}

		return InteractionResult.PASS;
	}

	private static void damageBroom(UseOnContext context, Player player) {
		if (player == null || player.getAbilities().instabuild) {
			return;
		}
		context.getItemInHand().hurtAndBreak(
			1,
			player,
			context.getHand() == InteractionHand.MAIN_HAND
				? EquipmentSlot.MAINHAND
				: EquipmentSlot.OFFHAND
		);
	}
}
