package pl.peterwolf.echoesinink.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import pl.peterwolf.echoesinink.util.PrintPlacement;
import pl.peterwolf.echoesinink.world.ChronicleClueService;

/**
 * Restored Chronicle of the Printer — progressive location clues (server-side).
 */
public class RestoredChroniclePageItem extends Item {
	public RestoredChroniclePageItem(Properties properties) {
		super(properties);
	}

	/** Lay the chronicle on a table/floor — renders like the press impression. */
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
		ItemStack stack = player.getItemInHand(hand);
		boolean ok = ChronicleClueService.read(serverPlayer, stack);
		return ok ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
	}

	@Override
	public void appendHoverText(
		ItemStack stack,
		TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> tooltip,
		TooltipFlag flag
	) {
		tooltip.accept(Component.translatable("item.echoes_in_ink.restored_chronicle_page.desc").withColor(0xAAAAAA));
		tooltip.accept(Component.translatable("item.echoes_in_ink.restored_chronicle_page.use").withColor(0xC0C0FF));
		tooltip.accept(Component.translatable("item.echoes_in_ink.archive_page.place_hint").withColor(0xC0C0FF));
	}
}
