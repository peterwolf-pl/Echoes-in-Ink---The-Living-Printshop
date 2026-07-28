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
 * Server-persisted, selectively synced archive progress for one player.
 * Only entry id strings — no executable content.
 */
public final class PlayerArchive {
	public static final Codec<PlayerArchive> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.STRING.listOf().optionalFieldOf("unlocked", List.of()).forGetter(PlayerArchive::unlockedList)
		).apply(instance, PlayerArchive::fromList)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, PlayerArchive> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
		PlayerArchive::unlockedList,
		PlayerArchive::fromList
	);

	private final Set<String> unlocked = new HashSet<>();

	public PlayerArchive() {}

	private PlayerArchive(Set<String> unlocked) {
		this.unlocked.addAll(unlocked);
	}

	private static PlayerArchive fromList(List<String> list) {
		return new PlayerArchive(new HashSet<>(list));
	}

	private List<String> unlockedList() {
		return List.copyOf(unlocked);
	}

	public boolean has(String id) {
		return unlocked.contains(id);
	}

	public boolean has(ArchiveEntries.Def def) {
		return has(def.id());
	}

	/** @return true if newly unlocked */
	public boolean unlock(String id) {
		return unlocked.add(id);
	}

	public void clear() {
		unlocked.clear();
	}

	public Set<String> unlockedIds() {
		return Set.copyOf(unlocked);
	}

	public int count() {
		return unlocked.size();
	}

	/** Immutable snapshot for sync/setAttached. */
	public PlayerArchive copy() {
		return new PlayerArchive(unlocked);
	}
}
