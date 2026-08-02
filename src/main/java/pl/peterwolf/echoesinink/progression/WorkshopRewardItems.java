package pl.peterwolf.echoesinink.progression;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pl.peterwolf.echoesinink.item.ModItems;

/** Converts pure deterministic reward definitions to registered server item stacks. */
public final class WorkshopRewardItems {
	private WorkshopRewardItems() {}

	public static ItemStack createStack(RewardStack reward) {
		Item item = switch (reward.kind()) {
			case PRESS_SCREW -> ModItems.PRESS_SCREW;
			case PRESS_HANDLE -> ModItems.PRESS_HANDLE;
			case PRESS_PLATEN -> ModItems.PRESS_PLATEN;
			case PRESS_CARRIAGE -> ModItems.PRESS_CARRIAGE;
			case WOODEN_MATRIX -> ModItems.WOODEN_PRINTING_MATRIX;
			case DAMAGED_PAGE -> ModItems.DAMAGED_ARCHIVE_PAGE;
			case BLANK_PAGE -> ModItems.BLANK_ARCHIVE_PAGE;
			case INK_BALL -> ModItems.INK_BALL;
			case INK_PAD -> ModItems.INK_PAD;
			case INSTRUCTION_SHEET -> ModItems.PRINTERS_INSTRUCTION_SHEET;
			case MAP_FRAGMENT -> ModItems.WORKSHOP_MAP_FRAGMENT;
			case METAL_TYPE -> ModItems.METAL_TYPE_PIECE;
			case CHARCOAL_RUBBING_PAPER -> ModItems.CHARCOAL_RUBBING_PAPER;
			case UPPER_MATRIX_FRAGMENT -> ModItems.UPPER_MATRIX_FRAGMENT;
			case LOWER_MATRIX_FRAGMENT -> ModItems.LOWER_MATRIX_FRAGMENT;
			case MISSING_LETTER_INSERT -> ModItems.MISSING_LETTER_INSERT;
			case VILLAGE_CHRONICLE_MATRIX -> ModItems.VILLAGE_CHRONICLE_MATRIX;
			case LEAD_TYPE_SET -> ModItems.LEAD_TYPE_SET;
			case IRON_CHASE -> ModItems.IRON_CHASE;
			case MISSING_HEADLINE_TYPE -> ModItems.MISSING_HEADLINE_TYPE;
			case PRINTERS_NOTES -> ModItems.PRINTERS_NOTES;
			case FORBIDDEN_NOTICE_FORME -> ModItems.FORBIDDEN_NOTICE_FORME;
		};
		return new ItemStack(item, reward.count());
	}
}
