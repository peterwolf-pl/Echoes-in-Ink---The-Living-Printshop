package pl.peterwolf.echoesinink.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import pl.peterwolf.echoesinink.util.PrintPlacement;

/**
 * Workshop matrix or machine part that can be laid on a table / floor top face.
 */
public class PlaceableOnSurfaceItem extends Item {
	private final String descKey;

	public PlaceableOnSurfaceItem(Properties properties, String descKey) {
		super(properties);
		this.descKey = descKey;
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
		tooltip.accept(Component.translatable(descKey).withColor(0xAAAAAA));
		tooltip.accept(Component.translatable("item.echoes_in_ink.place_on_surface.hint").withColor(0xC0C0FF));
	}
}
