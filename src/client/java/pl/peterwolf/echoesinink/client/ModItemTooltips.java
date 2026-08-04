package pl.peterwolf.echoesinink.client;

import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import pl.peterwolf.echoesinink.EchoesInInk;

/** Adds localized description and gameplay-purpose lines to every mod stack. */
public final class ModItemTooltips {
	private ModItemTooltips() {}

	public static void init() {
		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> append(stack, lines));
	}

	private static void append(ItemStack stack, List<Component> lines) {
		var itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
		if (!itemId.getNamespace().equals(EchoesInInk.MOD_ID)) {
			return;
		}
		String baseKey = stack.getItem().getDescriptionId();
		appendIfPresent(lines, baseKey + ".desc", 0xAAAAAA);
		appendIfPresent(lines, baseKey + ".purpose", 0xFFD27F);
	}

	private static void appendIfPresent(List<Component> lines, String key, int color) {
		if (containsTranslation(lines, key)) {
			return;
		}
		lines.add(Component.translatable(key).withColor(color));
	}

	private static boolean containsTranslation(List<Component> lines, String key) {
		return lines.stream().anyMatch(line ->
			line.getContents() instanceof TranslatableContents translated
				&& translated.getKey().equals(key)
		);
	}
}
