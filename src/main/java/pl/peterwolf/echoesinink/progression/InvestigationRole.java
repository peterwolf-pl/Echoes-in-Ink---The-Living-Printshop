package pl.peterwolf.echoesinink.progression;

import java.util.Locale;

/**
 * Stable semantic locations inside a printshop. Mandatory starter rewards are
 * allocated by role, never by an independent random roll.
 */
public enum InvestigationRole {
	PRESS_FRAME("press_frame"),
	MACHINE_REMAINS("machine_remains"),
	CELLAR_CACHE("cellar_cache"),
	FLOOR_CACHE("floor_cache"),
	MATRIX_BENCH("matrix_bench"),
	ARCHIVE_DESK("archive_desk"),
	INK_STATION("ink_station"),
	PLAQUE_CLUE("plaque_clue"),
	SUSPICIOUS_FLOOR("suspicious_floor");

	private final String id;

	InvestigationRole(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public static InvestigationRole byId(String id) {
		if (id != null) {
			for (InvestigationRole value : values()) {
				if (value.id.equals(id.toLowerCase(Locale.ROOT))) {
					return value;
				}
			}
		}
		return SUSPICIOUS_FLOOR;
	}
}
