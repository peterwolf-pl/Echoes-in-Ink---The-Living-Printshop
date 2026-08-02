package pl.peterwolf.echoesinink.block;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pl.peterwolf.echoesinink.item.ModItems;
import pl.peterwolf.echoesinink.config.ModConfig;

/**
 * Weighted server-side investigation loot. Never called from the client.
 */
public final class InvestigationLoot {
	public enum Profile {
		DEBRIS,
		TABLE,
		CABINET,
		SHELF,
		PRESS,
		FLOOR,
		FLOOR_HIDDEN,
		PLAQUE
	}

	public record Result(String id, ItemStack stack, Component message) {
		public ItemStack createStack() {
			return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
		}
	}

	private record Entry(String id, int weight, Item item, int count, String messageKey) {}

	private InvestigationLoot() {}

	public static Result roll(ServerLevel level, Profile profile) {
		List<Entry> table = tableFor(profile);
		int total = 0;
		for (Entry e : table) {
			total += e.weight;
		}
		RandomSource random = level.getRandom();
		int roll = random.nextInt(Math.max(1, total));
		int cursor = 0;
		for (Entry e : table) {
			cursor += e.weight;
			if (roll < cursor) {
				ItemStack stack = e.item == null ? ItemStack.EMPTY : new ItemStack(e.item, e.count);
				return new Result(e.id, stack, Component.translatable(e.messageKey));
			}
		}
		return nothing();
	}

	private static Result nothing() {
		return new Result(
			"nothing",
			ItemStack.EMPTY,
			Component.translatable("investigation.echoes_in_ink.nothing")
		);
	}

	private static List<Entry> tableFor(Profile profile) {
		List<Entry> list = new ArrayList<>();
		list.add(new Entry("nothing", 25, null, 0, "investigation.echoes_in_ink.nothing"));
		list.add(new Entry("historical_clue", 15, null, 0, "investigation.echoes_in_ink.clue"));

		switch (profile) {
			case DEBRIS -> {
				list.add(new Entry("metal_type", 30, ModItems.METAL_TYPE_PIECE, 1, "investigation.echoes_in_ink.metal_type"));
				list.add(new Entry("matrix_fragment", 15, ModItems.UPPER_MATRIX_FRAGMENT, 1, "investigation.echoes_in_ink.matrix_fragment"));
				list.add(new Entry("matrix_fragment", 10, ModItems.LOWER_MATRIX_FRAGMENT, 1, "investigation.echoes_in_ink.matrix_fragment"));
				list.add(new Entry("damaged_page", 5, ModItems.DAMAGED_ARCHIVE_PAGE, 1, "investigation.echoes_in_ink.damaged_page"));
			}
			case TABLE -> {
				list.add(new Entry("metal_type", 20, ModItems.METAL_TYPE_PIECE, 2, "investigation.echoes_in_ink.metal_type"));
				list.add(new Entry("ink_ball", 20, ModItems.INK_BALL, 1, "investigation.echoes_in_ink.ink"));
				list.add(new Entry("damaged_page", 15, ModItems.DAMAGED_ARCHIVE_PAGE, 1, "investigation.echoes_in_ink.damaged_page"));
				list.add(new Entry("hidden_compartment", 5, ModItems.BLANK_ARCHIVE_PAGE, 1, "investigation.echoes_in_ink.hidden"));
			}
			case CABINET -> {
				list.add(new Entry("metal_type", 40, ModItems.METAL_TYPE_PIECE, 3, "investigation.echoes_in_ink.metal_type"));
				list.add(new Entry("matrix_fragment", 20, ModItems.LEAD_TYPE_SET, 1, "investigation.echoes_in_ink.matrix_fragment"));
				list.add(new Entry("hidden_compartment", 10, ModItems.METAL_TYPE_PIECE, 5, "investigation.echoes_in_ink.hidden"));
			}
			case SHELF -> {
				list.add(new Entry("damaged_page", 35, ModItems.DAMAGED_ARCHIVE_PAGE, 1, "investigation.echoes_in_ink.damaged_page"));
				list.add(new Entry("blank_page", 20, ModItems.BLANK_ARCHIVE_PAGE, 2, "investigation.echoes_in_ink.blank_page"));
				list.add(new Entry("printers_notes", 8, ModItems.PRINTERS_NOTES, 1, "investigation.echoes_in_ink.hidden"));
			}
			case PRESS -> {
				list.add(new Entry("metal_type", 30, ModItems.METAL_TYPE_PIECE, 2, "investigation.echoes_in_ink.metal_type"));
				list.add(new Entry("matrix_fragment", 20, ModItems.MISSING_LETTER_INSERT, 1, "investigation.echoes_in_ink.matrix_fragment"));
				list.add(new Entry("ink_ball", 15, ModItems.INK_BALL, 2, "investigation.echoes_in_ink.ink"));
				if (ModConfig.INSTANCE.allowSparePressPartsInLaterRuins) {
					list.add(new Entry("press_screw", 1, ModItems.PRESS_SCREW, 1, "investigation.echoes_in_ink.press_part"));
					list.add(new Entry("press_platen", 1, ModItems.PRESS_PLATEN, 1, "investigation.echoes_in_ink.press_part"));
					list.add(new Entry("press_carriage", 1, ModItems.PRESS_CARRIAGE, 1, "investigation.echoes_in_ink.press_part"));
					list.add(new Entry("press_handle", 1, ModItems.PRESS_HANDLE, 1, "investigation.echoes_in_ink.press_part"));
				}
			}
			case FLOOR -> {
				list.add(new Entry("metal_type", 25, ModItems.METAL_TYPE_PIECE, 1, "investigation.echoes_in_ink.metal_type"));
				list.add(new Entry("ink_pad", 15, ModItems.INK_PAD, 1, "investigation.echoes_in_ink.ink"));
			}
			case FLOOR_HIDDEN -> {
				list.add(new Entry("matrix_fragment", 30, ModItems.MISSING_LETTER_INSERT, 1, "investigation.echoes_in_ink.matrix_fragment"));
				list.add(new Entry("ink_pad", 25, ModItems.INK_PAD, 2, "investigation.echoes_in_ink.ink"));
				list.add(new Entry("printers_notes", 20, ModItems.PRINTERS_NOTES, 1, "investigation.echoes_in_ink.hidden"));
			}
			case PLAQUE -> {
				list.add(new Entry("historical_clue", 50, null, 0, "investigation.echoes_in_ink.plaque_clue"));
				list.add(new Entry("hidden_compartment", 10, ModItems.METAL_TYPE_PIECE, 1, "investigation.echoes_in_ink.hidden"));
			}
		}
		return list;
	}
}
