package pl.peterwolf.echoesinink.progression;

/** Migration-safe structure identity shared by generation and NBT loading. */
public record WorkshopIdentity(String workshopId, WorkshopVariant variant, int layoutIndex) {
	public WorkshopIdentity {
		workshopId = workshopId == null || workshopId.isBlank() ? "printshop_unknown" : workshopId;
		variant = variant == null ? WorkshopVariant.RURAL_WOODCUT : variant;
		layoutIndex = Math.floorMod(layoutIndex, 2);
	}

	public static WorkshopIdentity fromStored(String workshopId, String variantId, int layoutIndex) {
		return new WorkshopIdentity(workshopId, WorkshopVariant.byId(variantId), layoutIndex);
	}
}
