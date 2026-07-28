package pl.peterwolf.echoesinink.echo;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Server-side ephemeral echo state. Never saved as world entities/blocks.
 */
public final class ActiveEcho {
	public final String id;
	public final ResourceKey<Level> dimension;
	public final BlockPos center;
	public final int durationTicks;
	public int tick;
	public int lastBeatIndex = -1;
	/** Players who have already received start for mid-join tracking. */
	public final Set<UUID> notifiedPlayers = new HashSet<>();

	public ActiveEcho(String id, ResourceKey<Level> dimension, BlockPos center, int durationTicks) {
		this.id = id;
		this.dimension = dimension;
		this.center = center.immutable();
		this.durationTicks = Math.max(100, durationTicks);
		this.tick = 0;
	}

	public float progress() {
		return Math.min(1.0F, tick / (float) durationTicks);
	}

	public boolean finished() {
		return tick >= durationTicks;
	}
}
