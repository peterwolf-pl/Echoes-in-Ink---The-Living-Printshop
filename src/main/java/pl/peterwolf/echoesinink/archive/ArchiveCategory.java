package pl.peterwolf.echoesinink.archive;

import net.minecraft.util.StringRepresentable;

public enum ArchiveCategory implements StringRepresentable {
	WORKSHOPS("workshops"),
	MACHINE_PARTS("machine_parts"),
	PRINTING_MATRICES("printing_matrices"),
	PRINTED_WORKS("printed_works"),
	HISTORICAL_ECHOES("historical_echoes"),
	UNRESOLVED_CLUES("unresolved_clues");

	private final String name;

	ArchiveCategory(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
