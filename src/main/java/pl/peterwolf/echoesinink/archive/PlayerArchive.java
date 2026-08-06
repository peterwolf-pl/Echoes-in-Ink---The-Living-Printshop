package pl.peterwolf.echoesinink.archive;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Server-persisted archive progress for one player.
 *
 * <p>Only opaque ids are synchronized, and Fabric's attachment predicate sends
 * this snapshot solely to its owning player. New tracking fields are optional
 * in the persistent codec so archives from 1.0.x worlds load safely.</p>
 */
public final class PlayerArchive {
	private static final Codec<Tracking> TRACKING_CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.STRING.listOf().optionalFieldOf("workshop_ids", List.of()).forGetter(Tracking::workshopIds),
			Codec.STRING.listOf().optionalFieldOf("workshop_variants", List.of()).forGetter(Tracking::workshopVariants),
			Codec.STRING.listOf().optionalFieldOf("recovered_materials", List.of()).forGetter(Tracking::recoveredMaterials),
			Codec.STRING.listOf().optionalFieldOf("available_recipes", List.of()).forGetter(Tracking::availableRecipes),
			Codec.STRING.listOf().optionalFieldOf("printed_works", List.of()).forGetter(Tracking::printedWorks),
			Codec.STRING.listOf().optionalFieldOf("unresolved_clues", List.of()).forGetter(Tracking::unresolvedClues)
		).apply(instance, Tracking::new)
	);

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static final StreamCodec<RegistryFriendlyByteBuf, List<String>> STRING_LIST_CODEC =
		(StreamCodec) ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list());

	private static final StreamCodec<RegistryFriendlyByteBuf, Tracking> TRACKING_STREAM_CODEC = StreamCodec.composite(
		STRING_LIST_CODEC, Tracking::workshopIds,
		STRING_LIST_CODEC, Tracking::workshopVariants,
		STRING_LIST_CODEC, Tracking::recoveredMaterials,
		STRING_LIST_CODEC, Tracking::availableRecipes,
		STRING_LIST_CODEC, Tracking::printedWorks,
		STRING_LIST_CODEC, Tracking::unresolvedClues,
		Tracking::new
	);

	public static final Codec<PlayerArchive> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.STRING.listOf().optionalFieldOf("unlocked", List.of()).forGetter(PlayerArchive::unlockedList),
			TRACKING_CODEC.optionalFieldOf("tracking", Tracking.EMPTY).forGetter(PlayerArchive::tracking)
		).apply(instance, PlayerArchive::fromStored)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, PlayerArchive> STREAM_CODEC = StreamCodec.composite(
		STRING_LIST_CODEC,
		PlayerArchive::unlockedList,
		TRACKING_STREAM_CODEC,
		PlayerArchive::tracking,
		PlayerArchive::fromStored
	);

	private final Set<String> unlocked = new HashSet<>();
	private final Set<String> workshopIds = new HashSet<>();
	private final Set<String> workshopVariants = new HashSet<>();
	private final Set<String> recoveredMaterials = new HashSet<>();
	private final Set<String> availableRecipes = new HashSet<>();
	private final Set<String> printedWorks = new HashSet<>();
	private final Set<String> unresolvedClues = new HashSet<>();

	public PlayerArchive() {}

	private static PlayerArchive fromStored(List<String> unlocked, Tracking tracking) {
		PlayerArchive archive = new PlayerArchive();
		archive.unlocked.addAll(unlocked);
		archive.workshopIds.addAll(tracking.workshopIds());
		archive.workshopVariants.addAll(tracking.workshopVariants());
		archive.recoveredMaterials.addAll(tracking.recoveredMaterials());
		archive.availableRecipes.addAll(tracking.availableRecipes());
		archive.printedWorks.addAll(tracking.printedWorks());
		archive.unresolvedClues.addAll(tracking.unresolvedClues());
		// Backfill category summaries for legacy archives that predate tracking.
		for (String entryId : archive.unlocked) {
			ArchiveEntries.byId(entryId).ifPresent(definition -> {
				if (definition.category() == ArchiveCategory.PRINTED_WORKS) {
					archive.printedWorks.add(entryId);
				} else if (definition.category() == ArchiveCategory.UNRESOLVED_CLUES) {
					archive.unresolvedClues.add(entryId);
				}
			});
		}
		return archive;
	}

	private List<String> unlockedList() {
		return sorted(unlocked);
	}

	private Tracking tracking() {
		return new Tracking(
			sorted(workshopIds),
			sorted(workshopVariants),
			sorted(recoveredMaterials),
			sorted(availableRecipes),
			sorted(printedWorks),
			sorted(unresolvedClues)
		);
	}

	private static List<String> sorted(Set<String> values) {
		return values.stream().sorted().toList();
	}

	public boolean has(String id) {
		return unlocked.contains(id);
	}

	public boolean has(ArchiveEntries.Def def) {
		return has(def.id());
	}

	/** @return true if newly unlocked */
	public boolean unlock(String id) {
		return addSafe(unlocked, id);
	}

	public boolean recordWorkshop(String workshopId, String variantId) {
		boolean changed = addSafe(workshopIds, workshopId);
		return addSafe(workshopVariants, variantId) || changed;
	}

	public boolean recordRecoveredMaterial(String itemId) {
		return addSafe(recoveredMaterials, itemId);
	}

	public boolean recordAvailableRecipe(String recipeId) {
		return addSafe(availableRecipes, recipeId);
	}

	public boolean recordPrintedWork(String itemId) {
		return addSafe(printedWorks, itemId);
	}

	public boolean recordUnresolvedClue(String clueId) {
		return addSafe(unresolvedClues, clueId);
	}

	private static boolean addSafe(Set<String> target, String id) {
		return id != null && !id.isBlank() && target.add(id);
	}

	public void clear() {
		unlocked.clear();
		workshopIds.clear();
		workshopVariants.clear();
		recoveredMaterials.clear();
		availableRecipes.clear();
		printedWorks.clear();
		unresolvedClues.clear();
	}

	public Set<String> unlockedIds() { return Set.copyOf(unlocked); }
	public Set<String> workshopIds() { return Set.copyOf(workshopIds); }
	public Set<String> workshopVariants() { return Set.copyOf(workshopVariants); }
	public Set<String> recoveredMaterials() { return Set.copyOf(recoveredMaterials); }
	public Set<String> availableRecipes() { return Set.copyOf(availableRecipes); }
	public Set<String> printedWorks() { return Set.copyOf(printedWorks); }
	public Set<String> unresolvedClues() { return Set.copyOf(unresolvedClues); }

	public int count() {
		return unlocked.size();
	}

	/** Immutable-by-convention snapshot used by attachment sync. */
	public PlayerArchive copy() {
		return fromStored(unlockedList(), tracking());
	}

	private record Tracking(
		List<String> workshopIds,
		List<String> workshopVariants,
		List<String> recoveredMaterials,
		List<String> availableRecipes,
		List<String> printedWorks,
		List<String> unresolvedClues
	) {
		private static final Tracking EMPTY = new Tracking(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

		private Tracking {
			workshopIds = List.copyOf(workshopIds);
			workshopVariants = List.copyOf(workshopVariants);
			recoveredMaterials = List.copyOf(recoveredMaterials);
			availableRecipes = List.copyOf(availableRecipes);
			printedWorks = List.copyOf(printedWorks);
			unresolvedClues = List.copyOf(unresolvedClues);
		}
	}
}
