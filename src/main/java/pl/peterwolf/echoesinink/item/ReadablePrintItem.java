package pl.peterwolf.echoesinink.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import pl.peterwolf.echoesinink.sound.ModSounds;
import pl.peterwolf.echoesinink.util.PrintPlacement;

/**
 * Printed page/print that can be read (server chat) when used.
 * Body lines use keys {@code printId + ".line1"} … while present in lang.
 */
public class ReadablePrintItem extends Item {
	private final String titleKey;
	private final String descKey;
	private final String bodyPrefix;
	private final int lineCount;

	public ReadablePrintItem(Properties properties, String id, int lineCount) {
		super(properties);
		this.titleKey = "item.echoes_in_ink." + id;
		this.descKey = "item.echoes_in_ink." + id + ".desc";
		this.bodyPrefix = "print.echoes_in_ink." + id;
		this.lineCount = Math.max(1, lineCount);
	}

	/** Lay the print on a top surface (same look as on the press). */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		return PrintPlacement.tryLayOnTop(context);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}
		showPrint(serverPlayer);
		level.playSound(null, player.blockPosition(), ModSounds.PRESS_COLLECT, SoundSource.PLAYERS, 0.5F, 1.1F);
		return InteractionResult.SUCCESS_SERVER;
	}

	public void showPrint(ServerPlayer player) {
		player.sendSystemMessage(Component.translatable(titleKey).withStyle(ChatFormatting.GOLD));
		for (int i = 1; i <= lineCount; i++) {
			player.sendSystemMessage(
				Component.translatable(bodyPrefix + ".line" + i).withStyle(ChatFormatting.GRAY)
			);
		}
		player.sendOverlayMessage(Component.translatable("print.echoes_in_ink.reading"));
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
		tooltip.accept(Component.translatable("print.echoes_in_ink.use_hint").withColor(0xC0C0FF));
		tooltip.accept(Component.translatable("item.echoes_in_ink.archive_page.place_hint").withColor(0xC0C0FF));
	}
}
