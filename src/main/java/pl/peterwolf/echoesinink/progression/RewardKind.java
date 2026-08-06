package pl.peterwolf.echoesinink.progression;

/** Logical reward ids kept free of Minecraft registry dependencies for tests. */
public enum RewardKind {
	PRESS_SCREW,
	PRESS_HANDLE,
	PRESS_PLATEN,
	PRESS_CARRIAGE,
	WOODEN_MATRIX,
	DAMAGED_PAGE,
	BLANK_PAGE,
	INK_BALL,
	INK_PAD,
	INSTRUCTION_SHEET,
	MAP_FRAGMENT,
	METAL_TYPE,
	CHARCOAL_RUBBING_PAPER,
	VILLAGE_CHRONICLE_MATRIX,
	LEAD_TYPE_SET,
	IRON_CHASE,
	MISSING_HEADLINE_TYPE,
	PRINTERS_NOTES,
	FORBIDDEN_NOTICE_FORME;

	public boolean isRequiredPressPart() {
		return this == PRESS_SCREW || this == PRESS_HANDLE || this == PRESS_PLATEN || this == PRESS_CARRIAGE;
	}
}
