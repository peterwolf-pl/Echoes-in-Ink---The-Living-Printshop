package pl.peterwolf.echoesinink.block;

import net.minecraft.util.StringRepresentable;

/**
 * Server-authoritative press cycle phase. Client only interpolates visuals from this.
 */
public enum PressPhase implements StringRepresentable {
	/** Frame placed; missing screw/handle/platen/carriage. */
	INCOMPLETE("incomplete"),
	/** Assembled, carriage out, waiting for inputs / load. */
	IDLE("idle"),
	/** Carriage slid under the platen. */
	CARRIAGE_IN("carriage_in"),
	/** Handle pulled — platen descending. */
	PRESSING("pressing"),
	/** Impression done; handle/platen returning. */
	RESETTING("resetting"),
	/** Print finished on carriage; pull carriage out. */
	IMPRESSION_DONE("impression_done"),
	/** Output ready to collect from tray. */
	OUTPUT_READY("output_ready"),
	/** Soft jam (missing step / bad timing). Cleared by empty-hand reset. */
	JAMMED("jammed");

	private final String name;

	PressPhase(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public static PressPhase byName(String name) {
		for (PressPhase phase : values()) {
			if (phase.name.equals(name)) {
				return phase;
			}
		}
		return INCOMPLETE;
	}
}
