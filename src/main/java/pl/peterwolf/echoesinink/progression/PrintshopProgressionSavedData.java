package pl.peterwolf.echoesinink.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import pl.peterwolf.echoesinink.EchoesInInk;

/** World-global starter claim shared by multiplayer groups and persisted on disk. */
public final class PrintshopProgressionSavedData extends SavedData {
	private static final Codec<PrintshopProgressionSavedData> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.STRING.optionalFieldOf("starter_workshop_id", "")
				.forGetter(PrintshopProgressionSavedData::starterWorkshopId)
		).apply(instance, PrintshopProgressionSavedData::new)
	);

	private static final SavedDataType<PrintshopProgressionSavedData> TYPE = new SavedDataType<>(
		EchoesInInk.id("printshop_progression"),
		PrintshopProgressionSavedData::new,
		CODEC,
		DataFixTypes.LEVEL
	);

	private String starterWorkshopId = "";

	public PrintshopProgressionSavedData() {}

	private PrintshopProgressionSavedData(String starterWorkshopId) {
		this.starterWorkshopId = normalize(starterWorkshopId);
	}

	public static PrintshopProgressionSavedData get(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	/**
	 * The first structure-bound investigation completed on the server claims the
	 * starter role. The claim is atomic on the server thread and never rerolled.
	 */
	public synchronized boolean claimAndIsStarter(String workshopId) {
		String normalized = normalize(workshopId);
		if (normalized.isEmpty()) {
			return false;
		}
		if (starterWorkshopId.isEmpty()) {
			starterWorkshopId = normalized;
			setDirty();
		}
		return starterWorkshopId.equals(normalized);
	}

	public String starterWorkshopId() {
		return starterWorkshopId;
	}

	private static String normalize(String workshopId) {
		return workshopId == null ? "" : workshopId.trim();
	}
}
