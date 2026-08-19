package pl.peterwolf.echoesinink.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import pl.peterwolf.echoesinink.client.animation.EchoGhostAnimator;
import pl.peterwolf.echoesinink.config.ModConfig;

/**
 * Client-side dust and ghost-worker animation for active echoes.
 * Figures are particle silhouettes, never entities.
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
			double cy = view.center.getY();
			double cz = view.center.getZ() + 0.5;
			float cycle = view.tick * 0.42F;

			if (!reduced && level.getGameTime() % 2 == 0) {
				level.addParticle(
					ParticleTypes.ASH,
					cx + (level.getRandom().nextDouble() - 0.5) * 3.2,
					cy + 0.4 + level.getRandom().nextDouble() * 1.8,
					cz + (level.getRandom().nextDouble() - 0.5) * 3.2,
					0.0, 0.01, 0.0
				);
			}

			drawPrinter(level, view, cx, cy, cz, cycle, p, reduced);
			drawHelper(level, view, cx, cy, cz, cycle, p, reduced);
			drawPressWork(level, cx, cy, cz, p);
		}
	}

	private static void drawPrinter(
		Level level,
		ClientEchoState.EchoView view,
		double cx,
		double cy,
		double cz,
		float cycle,
		float p,
		boolean reduced
	) {
		if (p < 0.18F || p >= 0.96F) {
			return;
		}
		float appear = fade(p, 0.18F, 0.24F, 0.85F, 0.95F);
		float hide = Mth.clamp((p - 0.72F) / 0.13F, 0.0F, 1.0F);
		boolean walking = p >= 0.45F && p < 0.72F;
		double x = cx - 1.55;
		double z = cz - 1.15;
		float yaw = EchoGhostAnimator.yawTowards(1.4, 1.0);
		if (p >= 0.45F && p < 0.72F) {
			float step = Mth.clamp((p - 0.45F) / 0.16F, 0.0F, 1.0F);
			x = Mth.lerp(step, cx - 1.55, cx - 0.35);
			z = Mth.lerp(step, cz - 1.15, cz - 0.15);
		} else if (hide > 0.0F) {
			x = Mth.lerp(hide, cx - 0.35, cx + 1.15);
			z = Mth.lerp(hide, cz - 0.15, cz - 0.85);
			yaw = EchoGhostAnimator.yawTowards(1.5, -0.7);
			walking = true;
		}
		EchoGhostAnimator.draw(
			level, x, cy, z, yaw, cycle,
			hide * 0.85F, appear, walking, false, reduced
		);
	}

	private static void drawHelper(
		Level level,
		ClientEchoState.EchoView view,
		double cx,
		double cy,
		double cz,
		float cycle,
		float p,
		boolean reduced
	) {
		if (p < 0.32F || p >= 0.96F) {
			return;
		}
		float appear = fade(p, 0.32F, 0.38F, 0.85F, 0.95F);
		float hide = Mth.clamp((p - 0.72F) / 0.13F, 0.0F, 1.0F);
		double startX = cx + 1.85;
		double startZ = cz + 1.55;
		double pressX = cx + 0.45;
		double pressZ = cz + 0.35;
		float walk = Mth.clamp((p - 0.32F) / 0.20F, 0.0F, 1.0F);
		double x = Mth.lerp(walk, startX, pressX);
		double z = Mth.lerp(walk, startZ, pressZ);
		float yaw = EchoGhostAnimator.yawTowards(pressX - startX, pressZ - startZ);
		boolean walking = walk < 1.0F && hide <= 0.0F;
		boolean carrying = p < 0.72F;
		if (hide > 0.0F) {
			x = Mth.lerp(hide, pressX, cx + 1.15);
			z = Mth.lerp(hide, pressZ, cz - 0.85);
			yaw = EchoGhostAnimator.yawTowards(0.7, -1.2);
			walking = true;
			carrying = false;
		}
		EchoGhostAnimator.draw(
			level, x, cy, z, yaw, cycle * 1.15F,
			hide * 0.85F, appear, walking, carrying, reduced
		);
	}

	private static void drawPressWork(Level level, double cx, double cy, double cz, float p) {
		if (p < 0.45F || p >= 0.72F || level.getGameTime() % 2 != 0) {
			return;
		}
		level.addParticle(ParticleTypes.CRIT, cx, cy + 1.25, cz, 0.0, 0.04, 0.0);
		if (p >= 0.62F && p < 0.70F) {
			level.addParticle(ParticleTypes.SMOKE, cx + 0.2, cy + 0.9, cz, 0.0, 0.03, 0.0);
		}
	}

	private static float fade(float p, float inStart, float inEnd, float outStart, float outEnd) {
		if (p < inStart) {
			return 0.0F;
		}
		if (p < inEnd) {
			return (p - inStart) / (inEnd - inStart);
		}
		if (p < outStart) {
			return 1.0F;
		}
		if (p < outEnd) {
			return 1.0F - (p - outStart) / (outEnd - outStart);
		}
		return 0.0F;
	}
}
