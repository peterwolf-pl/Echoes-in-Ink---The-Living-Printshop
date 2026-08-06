package pl.peterwolf.echoesinink.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
				.forGetter(PrintshopProgressionSavedData::starterWorkshopId),
			Codec.BOOL.optionalFieldOf("basic_press_operated", false)
				.forGetter(PrintshopProgressionSavedData::basicPressOperated),
			Codec.STRING.listOf().optionalFieldOf("starter_supply_workshops", List.of())
				.forGetter(data -> List.copyOf(data.starterSupplyWorkshops))
		).apply(instance, PrintshopProgressionSavedData::new)
	);

	private static final SavedDataType<PrintshopProgressionSavedData> TYPE = new SavedDataType<>(
		EchoesInInk.id("printshop_progression"),
		PrintshopProgressionSavedData::new,
		CODEC,
		DataFixTypes.LEVEL
	);

	private String starterWorkshopId = "";
	private boolean basicPressOperated;
	private final Set<String> starterSupplyWorkshops = new LinkedHashSet<>();

	public PrintshopProgressionSavedData() {}

	private PrintshopProgressionSavedData(
		String starterWorkshopId,
		boolean basicPressOperated,
		List<String> starterSupplyWorkshops
	) {
		this.starterWorkshopId = normalize(starterWorkshopId);
		this.basicPressOperated = basicPressOperated;
		for (String workshopId : starterSupplyWorkshops) {
			String normalized = normalize(workshopId);
			if (!normalized.isEmpty()) {
				this.starterSupplyWorkshops.add(normalized);
			}
		}
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

	/**
	 * Starter rewards remain available in every investigated workshop until the
	 * server has actually operated a complete press. This prevents an abandoned
	 * first ruin from permanently stranding a world without mandatory parts.
	 */
	public synchronized boolean starterRewardsAllowed(String workshopId) {
		claimAndIsStarter(workshopId);
		return !basicPressOperated && !normalize(workshopId).isEmpty();
	}

	public synchronized void markBasicPressOperated() {
		if (!basicPressOperated) {
			basicPressOperated = true;
			setDirty();
		}
	}

	/** Reserves one physical starter supply chest per workshop before the first press run. */
	public synchronized boolean claimStarterSupply(String workshopId) {
		String normalized = normalize(workshopId);
		if (basicPressOperated || normalized.isEmpty() || !starterSupplyWorkshops.add(normalized)) {
			return false;
		}
		setDirty();
		return true;
	}

	public synchronized boolean hasStarterSupply(String workshopId) {
		return starterSupplyWorkshops.contains(normalize(workshopId));
	}

	public boolean basicPressOperated() {
		return basicPressOperated;
	}

	public String starterWorkshopId() {
		return starterWorkshopId;
	}

	private static String normalize(String workshopId) {
		return workshopId == null ? "" : workshopId.trim();
	}
}
