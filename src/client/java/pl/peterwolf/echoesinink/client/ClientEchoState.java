package pl.peterwolf.echoesinink.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

/** Client-only active echo presentation state. */
public final class ClientEchoState {
	public static final class EchoView {
		public final String id;
		public final BlockPos center;
		public final int durationTicks;
		public int tick;
		public int beatIndex = -1;
		public float tintStrength;

		public EchoView(String id, BlockPos center, int durationTicks, int startTick) {
			this.id = id;
			this.center = center.immutable();
			this.durationTicks = Math.max(1, durationTicks);
			this.tick = startTick;
		}

		public float progress() {
			return Mth.clamp(tick / (float) durationTicks, 0.0F, 1.0F);
		}
	}

	private static final Map<String, EchoView> ACTIVE = new HashMap<>();

	private ClientEchoState() {}

	public static void start(String id, BlockPos center, int duration, int startTick) {
		ACTIVE.put(id, new EchoView(id, center, duration, startTick));
	}

	public static void sync(String id, int tick, int beatIndex) {
		EchoView view = ACTIVE.get(id);
		if (view != null) {
			// Do not rewind a slightly-ahead client clock; that makes ghosts hitch.
			if (tick > view.tick || view.tick - tick > 12) {
				view.tick = tick;
			}
			view.beatIndex = beatIndex;
		}
	}

	public static void end(String id) {
		ACTIVE.remove(id);
	}

	public static void clear() {
		ACTIVE.clear();
	}

	public static Map<String, EchoView> active() {
		return ACTIVE;
	}

	public static void clientTick() {
		for (EchoView view : ACTIVE.values()) {
			// Local interpolate between sync packets
			view.tick = Math.min(view.tick + 1, view.durationTicks);
			float p = view.progress();
			// Soft vignette strength early, fades near end
			if (p < 0.1F) {
				view.tintStrength = p / 0.1F * 0.35F;
			} else if (p > 0.85F) {
				view.tintStrength = (1.0F - p) / 0.15F * 0.35F;
			} else {
				view.tintStrength = 0.35F;
			}
		}
	}
}
