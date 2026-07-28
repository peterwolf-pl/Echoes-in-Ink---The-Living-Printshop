package pl.peterwolf.echoesinink.block;

import net.minecraft.util.StringRepresentable;

public enum InvestigationState implements StringRepresentable {
	UNTOUCHED("untouched"),
	PARTIALLY_CLEANED("partially_cleaned"),
	FULLY_INVESTIGATED("fully_investigated");

	private final String name;

	InvestigationState(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public InvestigationState next() {
		return switch (this) {
			case UNTOUCHED -> PARTIALLY_CLEANED;
			case PARTIALLY_CLEANED, FULLY_INVESTIGATED -> FULLY_INVESTIGATED;
		};
	}

	public boolean canClean() {
		return this != FULLY_INVESTIGATED;
	}
}
