package pl.peterwolf.echoesinink.progression;

/** Immutable logical reward stack used by the deterministic allocator. */
public record RewardStack(RewardKind kind, int count) {
	public RewardStack {
		if (kind == null) {
			throw new IllegalArgumentException("kind");
		}
		if (count < 1) {
			throw new IllegalArgumentException("count must be positive");
		}
	}
}
