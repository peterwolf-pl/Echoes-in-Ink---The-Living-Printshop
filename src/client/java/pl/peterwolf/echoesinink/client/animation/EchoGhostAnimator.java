package pl.peterwolf.echoesinink.client.animation;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * Humanoid ghost silhouettes built from short-lived particles. No entities.
 */
public final class EchoGhostAnimator {
	private EchoGhostAnimator() {}

	public static void draw(
		Level level,
		double x,
		double y,
		double z,
		float yaw,
		float cycle,
		float crouch,
		float alpha,
		boolean walking,
		boolean carrying,
		boolean reduced
	) {
		if (alpha < 0.06F) {
			return;
		}
		float bob = walking ? Mth.abs(Mth.sin(cycle)) * 0.05F : Mth.sin(cycle * 0.5F) * 0.02F;
		float squat = crouch * 0.5F;
		double baseY = y + bob - squat;
		float stride = walking ? 0.24F : 0.06F;
		float swing = walking ? 0.20F : 0.10F;
		float leg = Mth.sin(cycle) * stride;
		float arm = -Mth.sin(cycle) * swing;

		dot(level, x, baseY + 1.68, z, alpha, true);
		dot(level, x, baseY + 1.52, z, alpha, false);
		body(level, x, baseY, z, yaw, 1.28F, 0.0F, 0.0F, alpha);
		body(level, x, baseY, z, yaw, 1.08F, 0.0F, 0.0F, alpha);
		body(level, x, baseY, z, yaw, 0.88F, 0.0F, 0.0F, alpha);
		if (!reduced) {
			body(level, x, baseY, z, yaw, 1.18F, 0.08F, 0.0F, alpha * 0.7F);
			body(level, x, baseY, z, yaw, 1.18F, -0.08F, 0.0F, alpha * 0.7F);
		}

		limb(level, x, baseY, z, yaw, 1.28F, -0.22F, arm, alpha);
		limb(level, x, baseY, z, yaw, 1.28F, 0.22F, carrying ? 0.12F : -arm, alpha);
		if (carrying) {
			double[] page = rotate(yaw, 0.28F, 0.22F);
			level.addParticle(
				ParticleTypes.ENCHANT,
				x + page[0],
				baseY + 1.22,
				z + page[1],
				0.0, 0.01, 0.0
			);
		}

		limb(level, x, baseY, z, yaw, 0.52F, -0.10F, leg, alpha);
		limb(level, x, baseY, z, yaw, 0.22F, -0.10F, leg * 1.15F, alpha);
		limb(level, x, baseY, z, yaw, 0.52F, 0.10F, -leg, alpha);
		limb(level, x, baseY, z, yaw, 0.22F, 0.10F, -leg * 1.15F, alpha);
	}

	private static void body(
		Level level,
		double x,
		double y,
		double z,
		float yaw,
		float ly,
		float lx,
		float lz,
		float alpha
	) {
		double[] xz = rotate(yaw, lz, lx);
		dot(level, x + xz[0], y + ly, z + xz[1], alpha, false);
	}

	private static void limb(
		Level level,
		double x,
		double y,
		double z,
		float yaw,
		float ly,
		float lx,
		float forward,
		float alpha
	) {
		double[] xz = rotate(yaw, forward, lx);
		dot(level, x + xz[0], y + ly, z + xz[1], alpha, false);
	}

	private static void dot(Level level, double x, double y, double z, float alpha, boolean soul) {
		if (alpha < 1.0F && level.getRandom().nextFloat() > alpha) {
			return;
		}
		level.addParticle(ParticleTypes.WHITE_ASH, x, y, z, 0.0, 0.0, 0.0);
		if (soul && level.getRandom().nextFloat() < 0.55F) {
			level.addParticle(ParticleTypes.SOUL, x, y + 0.02, z, 0.0, 0.01, 0.0);
		}
	}

	/** {@code yaw} 0 looks toward +Z (south). */
	public static double[] rotate(float yaw, float forward, float right) {
		float sin = Mth.sin(yaw);
		float cos = Mth.cos(yaw);
		return new double[] {right * cos - forward * sin, right * sin + forward * cos};
	}

	public static float yawTowards(double dx, double dz) {
		return (float) Mth.atan2(dx, dz);
	}
}
