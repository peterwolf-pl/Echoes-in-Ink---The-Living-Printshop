package pl.peterwolf.echoesinink.item;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.gameevent.GameEvent;
import pl.peterwolf.echoesinink.block.LaidPaperBlock;
import pl.peterwolf.echoesinink.block.ModBlocks;
import pl.peterwolf.echoesinink.block.PaperKind;
import pl.peterwolf.echoesinink.sound.ModSounds;

/**
 * Blank / damaged archive page that can be laid on a flat top surface.
 */
public class PlaceableArchivePageItem extends Item {
	private final PaperKind kind;
	private final String descKey;

	public PlaceableArchivePageItem(Properties properties, PaperKind kind, String descKey) {
		super(properties);
		this.kind = kind;
		this.descKey = descKey;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getClickedFace() != Direction.UP) {
			return InteractionResult.PASS;
		}

		Level level = context.getLevel();
		BlockPos support = context.getClickedPos();
		BlockPos placePos = support.above();
		Player player = context.getPlayer();

		if (!level.getBlockState(support).isFaceSturdy(level, support, Direction.UP)) {
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

		BlockState state = ModBlocks.LAID_PAPER.defaultBlockState()
			.setValue(LaidPaperBlock.FACING, facing)
			.setValue(LaidPaperBlock.KIND, kind);

		if (!level.setBlock(placePos, state, 3)) {
			return InteractionResult.FAIL;
		}

		level.playSound(null, placePos, ModSounds.PRESS_COLLECT, SoundSource.BLOCKS, 0.45F, 1.25F);
		level.gameEvent(GameEvent.BLOCK_PLACE, placePos, GameEvent.Context.of(player, state));

		ItemStack stack = context.getItemInHand();
		if (player == null || !player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendOverlayMessage(Component.translatable("block.echoes_in_ink.laid_paper.placed"));
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override
	public void appendHoverText(
		ItemStack stack,
		TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> tooltip,
		TooltipFlag flag
	) {
		tooltip.accept(Component.translatable(descKey).withColor(0xAAAAAA));
		tooltip.accept(Component.translatable("item.echoes_in_ink.archive_page.place_hint").withColor(0xC0C0FF));
	}
}
