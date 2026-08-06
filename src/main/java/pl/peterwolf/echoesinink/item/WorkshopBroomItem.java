package pl.peterwolf.echoesinink.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;

/** Optional workshop cleanup tool that removes decorative cobwebs quickly. */
public final class WorkshopBroomItem extends Item {
	public WorkshopBroomItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockPos pos = context.getClickedPos();
		if (!context.getLevel().getBlockState(pos).is(Blocks.COBWEB)) {
			return InteractionResult.PASS;
		}
		Player player = context.getPlayer();
		if (context.getLevel() instanceof ServerLevel serverLevel) {
			serverLevel.destroyBlock(pos, true, player);
			serverLevel.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 0.8F, 0.8F);
			if (player != null && !player.getAbilities().instabuild) {
				context.getItemInHand().hurtAndBreak(
					1,
					player,
					context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
						? EquipmentSlot.MAINHAND
						: EquipmentSlot.OFFHAND
				);
			}
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}
}
