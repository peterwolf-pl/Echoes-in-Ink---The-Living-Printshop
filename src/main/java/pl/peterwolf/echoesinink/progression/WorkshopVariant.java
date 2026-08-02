package pl.peterwolf.echoesinink.progression;

import java.util.Locale;

/** Stable printshop identities stored in structure and investigation NBT. */
public enum WorkshopVariant {
	RURAL_WOODCUT("rural_woodcut"),
	TOWN_TYPE_FOUNDRY("town_type_foundry"),
	SCHOLARLY_ARCHIVE("scholarly_archive"),
	BURNED_CLANDESTINE("burned_clandestine");

	private final String id;

	WorkshopVariant(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public static WorkshopVariant byId(String id) {
		if (id != null) {
			for (WorkshopVariant value : values()) {
				if (value.id.equals(id.toLowerCase(Locale.ROOT))) {
					return value;
				}
			}
		}
		return RURAL_WOODCUT;
	}
}
