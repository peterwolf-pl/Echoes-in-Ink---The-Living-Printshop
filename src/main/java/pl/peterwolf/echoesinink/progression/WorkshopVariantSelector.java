package pl.peterwolf.echoesinink.progression;

/** Pure, stable selection of one major printshop type and one of two layouts. */
public final class WorkshopVariantSelector {
	public record Selection(WorkshopVariant variant, int layoutIndex) {
		public Selection {
			variant = variant == null ? WorkshopVariant.RURAL_WOODCUT : variant;
			layoutIndex = Math.floorMod(layoutIndex, 2);
		}
	}

	private WorkshopVariantSelector() {}

	public static Selection select(String workshopId, boolean variantsEnabled) {
		if (!variantsEnabled) {
			return new Selection(WorkshopVariant.RURAL_WOODCUT, 0);
		}
		long seed = WorkshopRewardAllocator.stableSeed(workshopId);
		WorkshopVariant[] variants = WorkshopVariant.values();
		WorkshopVariant variant = variants[(int) Math.floorMod(seed, variants.length)];
		int layout = (int) Math.floorMod(seed >>> 17, 2L);
		return new Selection(variant, layout);
	}
}
