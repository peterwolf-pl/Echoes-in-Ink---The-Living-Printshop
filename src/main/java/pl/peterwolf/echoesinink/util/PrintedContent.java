package pl.peterwolf.echoesinink.util;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.item.ModItems;

/** Shared mapping from print item → impression text key suffix (press + laid paper). */
public final class PrintedContent {
	private PrintedContent() {}

	@Nullable
	public static String impressionSuffix(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		if (stack.is(ModItems.PRINTERS_INSTRUCTION_SHEET)) {
			return "instruction";
		}
		if (stack.is(ModItems.RESTORED_CHRONICLE_PAGE)) {
			return "chronicle";
		}
		if (stack.is(ModItems.DECORATIVE_WOODCUT)) {
			return "woodcut";
		}
		if (stack.is(ModItems.PRINTED_WARNING_POSTER)) {
			return "warning";
		}
		if (stack.is(ModItems.WORKSHOP_MAP_FRAGMENT)) {
			return "map";
		}
		return null;
	}
}
