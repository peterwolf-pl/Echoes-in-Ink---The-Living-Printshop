package pl.peterwolf.echoesinink.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Simple item with a grey description tooltip. */
public class TooltipItem extends Item {
	private final String tooltipKey;

	public TooltipItem(Properties properties, String tooltipKey) {
		super(properties);
		this.tooltipKey = tooltipKey;
	}

	@Override
	public void appendHoverText(
		ItemStack stack,
		TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> tooltip,
		TooltipFlag flag
	) {
		tooltip.accept(Component.translatable(tooltipKey).withColor(0xAAAAAA));
	}
}
