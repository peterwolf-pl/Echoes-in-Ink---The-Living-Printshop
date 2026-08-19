package pl.peterwolf.echoesinink.structure;

/**
 * Village-adjacent printshop placement. Uses the same random-spread grid as
 * vanilla villages, then offsets each workshop to the village edge.
 */
public final class VillagePrintshopLayout {
	/** Must match {@code minecraft:worldgen/structure_set/villages}. */
	public static final int VILLAGE_SALT = 10387312;
	public static final int VILLAGE_SPACING = 34;
	public static final int VILLAGE_SEPARATION = 8;

	/** Distance from the village chunk center to a printshop origin. */
	public static final int EDGE_BLOCKS = 52;

	public static final int PRINTSHOP_WIDTH = 17;
	public static final int PRINTSHOP_DEPTH = 15;

	private VillagePrintshopLayout() {}

	/** Plains, meadow, savanna, and taiga villages are the larger vanilla ones. */
	public static boolean isLargeVillageBiome(String biomePath) {
		if (biomePath == null) {
			return false;
		}
		return switch (biomePath) {
			case "plains", "meadow", "savanna", "taiga" -> true;
			default -> false;
		};
	}

	public static int printshopCount(String biomePath) {
		return isLargeVillageBiome(biomePath) ? 2 : 1;
	}

	/**
	 * Origin offset from the village chunk center. Index 0 and 1 sit on
	 * opposite corners so two workshops do not overlap.
	 */
	public static int[] originOffset(int index, long seed) {
		int corner = Math.floorMod((int) seed + index * 2, 4);
		int east = (corner == 0 || corner == 1) ? EDGE_BLOCKS : -(EDGE_BLOCKS + PRINTSHOP_WIDTH);
		int south = (corner == 0 || corner == 3) ? EDGE_BLOCKS : -(EDGE_BLOCKS + PRINTSHOP_DEPTH);
		return new int[] {east, south};
	}
}
