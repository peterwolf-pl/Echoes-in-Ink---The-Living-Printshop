package pl.peterwolf.echoesinink.progression;

import net.minecraft.world.level.ChunkPos;

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

	/** Stable id shared by fresh generation and migration of pre-1.1 workshops. */
	public static String idForChunk(ChunkPos chunkPos) {
		return idForChunk(chunkPos, 0);
	}

	/** Distinct id for a second printshop sharing a village chunk. */
	public static String idForChunk(ChunkPos chunkPos, int index) {
		int hash = chunkPos.x() * 73856093 ^ chunkPos.z() * 19349663 ^ index * 83492791;
		return "printshop_" + Integer.toHexString(hash);
	}
}
