package pl.peterwolf.echoesinink.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import pl.peterwolf.echoesinink.config.ModConfig;
import pl.peterwolf.echoesinink.echo.EchoScript;

/**
 * Client-side dust / ghost actor particles for active echoes.
 * No real entities — only short-lived particles.
 */
public final class ClientEchoEffects {
	private ClientEchoEffects() {}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		LocalPlayer player = mc.player;
		if (level == null || player == null || ClientEchoState.active().isEmpty()) {
			return;
		}

		boolean reduced = ModConfig.INSTANCE.echoReducedParticles;
		for (ClientEchoState.EchoView view : ClientEchoState.active().values()) {
			float p = view.progress();
			double cx = view.center.getX() + 0.5;
			double cy = view.center.getY() + 0.1;
			double cz = view.center.getZ() + 0.5;

			if (!reduced && level.getGameTime() % 3 == 0) {
				int n = 3;
				for (int i = 0; i < n; i++) {
					level.addParticle(
						ParticleTypes.ASH,
						cx + (Math.random() - 0.5) * 3.0,
						cy + Math.random() * 2.0,
						cz + (Math.random() - 0.5) * 3.0,
						0, 0.01, 0
					);
				}
			}

			// Ghostly "workers" as end-rod / soul particle orbits (no entities)
			if (p >= 0.18F && p < 0.85F) {
				double t = view.tick * 0.08;
				// Printer near "type cabinet" offset
				double x1 = cx - 1.5 + Mth.sin((float) t) * 0.15;
				double z1 = cz - 1.2;
				double y1 = cy + 1.0 + Mth.sin((float) t * 2.0F) * 0.05;
				level.addParticle(ParticleTypes.END_ROD, x1, y1, z1, 0, 0.01, 0);
				level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x1, y1 - 0.4, z1, 0, 0.001, 0);

				if (p >= 0.32F) {
					// Second worker walking toward press (lerp path)
					float walk = Mth.clamp((p - 0.32F) / 0.2F, 0.0F, 1.0F);
					double x2 = cx + 1.8 - walk * 1.5;
					double z2 = cz + 1.5 - walk * 1.2;
					double y2 = cy + 1.0;
					level.addParticle(ParticleTypes.END_ROD, x2, y2, z2, 0, 0.01, 0);
					// "page" sparkle
					if (level.getGameTime() % 4 == 0) {
						level.addParticle(ParticleTypes.ENCHANT, x2, y2 + 0.3, z2, 0, 0.02, 0);
					}
				}
			}

			// Ghost press activity near center
			if (p >= 0.45F && p < 0.7F && level.getGameTime() % 2 == 0) {
				level.addParticle(ParticleTypes.CRIT, cx, cy + 1.2, cz, 0, 0.05, 0);
			}

			// Hide action near floor
			if (p >= 0.72F && p < 0.82F && level.getGameTime() % 3 == 0) {
				level.addParticle(
					ParticleTypes.SMOKE,
					cx + 1.2, cy + 0.2, cz - 0.8,
					0, 0.02, 0
				);
			}
		}
	}
}
