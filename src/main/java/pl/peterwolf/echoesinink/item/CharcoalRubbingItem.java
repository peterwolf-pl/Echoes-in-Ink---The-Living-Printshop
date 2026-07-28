package pl.peterwolf.echoesinink.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Rubbing result that displays its stored pattern identifier. */
public class CharcoalRubbingItem extends Item {
	public CharcoalRubbingItem(Properties properties) {
		super(properties);
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
	}
}
