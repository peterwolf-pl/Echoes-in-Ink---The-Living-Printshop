package pl.peterwolf.echoesinink.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import pl.peterwolf.echoesinink.util.PrintPlacement;

/** Rubbing result that displays its stored pattern identifier; can be laid on a surface. */
public class CharcoalRubbingItem extends Item {
	public CharcoalRubbingItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		return PrintPlacement.tryLayOnTop(context);
	}

	@Override
	public void appendHoverText(
		ItemStack stack,
		TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> tooltip,
		TooltipFlag flag
	) {
		tooltip.accept(Component.translatable("item.echoes_in_ink.charcoal_rubbing.desc").withColor(0xAAAAAA));
		Identifier pattern = stack.get(ModDataComponents.RUBBING_PATTERN);
		if (pattern != null) {
			tooltip.accept(Component.translatable("item.echoes_in_ink.charcoal_rubbing.pattern", pattern.toString())
				.withColor(0xC0C0FF));
		} else {
			tooltip.accept(Component.translatable("item.echoes_in_ink.charcoal_rubbing.no_pattern").withColor(0xFF5555));
		}
		tooltip.accept(Component.translatable("item.echoes_in_ink.place_on_surface.hint").withColor(0xC0C0FF));
	}
}
