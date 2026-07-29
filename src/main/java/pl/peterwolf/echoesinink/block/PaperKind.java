package pl.peterwolf.echoesinink.block;

import net.minecraft.util.StringRepresentable;

public enum PaperKind implements StringRepresentable {
	BLANK("blank"),
	DAMAGED("damaged");

	private final String name;

	PaperKind(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
