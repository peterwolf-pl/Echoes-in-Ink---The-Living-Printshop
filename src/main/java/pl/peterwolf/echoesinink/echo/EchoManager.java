package pl.peterwolf.echoesinink.echo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.archive.ArchiveEntries;
import pl.peterwolf.echoesinink.archive.ArchiveService;
import pl.peterwolf.echoesinink.config.ModConfig;
import pl.peterwolf.echoesinink.networking.EchoPayloads;

/**
 * Server-authoritative historical echoes. No permanent entities or blocks.
 * Clients only render visuals from packets.
 */
public final class EchoManager {
	private static final List<ActiveEcho> ACTIVE = new ArrayList<>();
	private static final double VIEW_RANGE = 48.0;

	private EchoManager() {}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(EchoManager::tickServer);
	}

	public static boolean isAnyActive() {
		return !ACTIVE.isEmpty();
	}

	public static boolean startLastPrintRun(ServerLevel level, BlockPos center, ServerPlayer trigger) {
		return start(level, center, EchoScript.LAST_PRINT_RUN, trigger);
	}

	public static boolean start(ServerLevel level, BlockPos center, String echoId, ServerPlayer trigger) {
		for (ActiveEcho existing : ACTIVE) {
			if (existing.dimension.equals(level.dimension()) && existing.center.closerThan(center, 16)) {
				if (trigger != null) {
					trigger.sendOverlayMessage(Component.translatable("echo.echoes_in_ink.already_active"));
				}
				return false;
			}
		}

		int duration = Math.max(100, ModConfig.INSTANCE.echoDurationTicks);
		ActiveEcho echo = new ActiveEcho(echoId, level.dimension(), center, duration);
		ACTIVE.add(echo);
		broadcastStart(level, echo);

		if (trigger != null) {
			trigger.sendOverlayMessage(Component.translatable("echo.echoes_in_ink.started"));
		}
		if (ModConfig.INSTANCE.debugLogging) {
			EchoesInInk.LOGGER.info("Started echo {} at {} for {} ticks", echoId, center, duration);
		}
		return true;
	}

	public static void trySkip(ServerPlayer player) {
		if (!ModConfig.INSTANCE.echoSkippableAfterFirstView) {
			player.sendOverlayMessage(Component.translatable("echo.echoes_in_ink.skip_disabled"));
			return;
		}
		boolean seen = ArchiveService.get(player).has(ArchiveEntries.ECHO_LAST_PRINT);
		// Ops may always skip for testing; others only after first completion.
		boolean isOp = player.getAbilities().instabuild;
		if (!seen && !isOp) {
			player.sendOverlayMessage(Component.translatable("echo.echoes_in_ink.skip_locked"));
			return;
		}

		ServerLevel level = player.level();
		ActiveEcho nearest = null;
		double best = Double.MAX_VALUE;
		for (ActiveEcho echo : ACTIVE) {
			if (!echo.dimension.equals(level.dimension())) {
				continue;
			}
			double d = echo.center.distToCenterSqr(player.position());
			if (d < best) {
				best = d;
				nearest = echo;
			}
		}
		if (nearest == null || best > VIEW_RANGE * VIEW_RANGE) {
			player.sendOverlayMessage(Component.translatable("echo.echoes_in_ink.nothing_to_skip"));
			return;
		}
		finish(level, nearest, true);
		player.sendOverlayMessage(Component.translatable("echo.echoes_in_ink.skipped"));
	}

	/** Mid-join / proximity sync for correct client state. */
	public static void syncPlayer(ServerPlayer player) {
		ServerLevel level = player.level();
		for (ActiveEcho echo : ACTIVE) {
			if (!echo.dimension.equals(level.dimension())) {
				continue;
			}
			if (echo.center.distToCenterSqr(player.position()) > VIEW_RANGE * VIEW_RANGE) {
				continue;
			}
			if (echo.notifiedPlayers.add(player.getUUID())) {
				EchoPayloads.sendStart(player, new EchoPayloads.EchoStartPayload(
					echo.id, echo.center, echo.durationTicks, echo.tick
				));
			}
			EchoPayloads.sendSync(player, new EchoPayloads.EchoSyncPayload(
				echo.id, echo.tick, echo.lastBeatIndex
			));
		}
	}

	private static void tickServer(MinecraftServer server) {
		if (ACTIVE.isEmpty()) {
			return;
		}
		Iterator<ActiveEcho> it = ACTIVE.iterator();
		while (it.hasNext()) {
			ActiveEcho echo = it.next();
			ServerLevel level = server.getLevel(echo.dimension);
			if (level == null) {
				it.remove();
				continue;
			}

			echo.tick++;
			float progress = echo.progress();

			// Advance scripted beats
			int beatIndex = echo.lastBeatIndex;
			for (int i = echo.lastBeatIndex + 1; i < EchoScript.LAST_PRINT_BEATS.size(); i++) {
				if (progress + 0.001F >= EchoScript.LAST_PRINT_BEATS.get(i).at()) {
					beatIndex = i;
					onBeat(level, echo, EchoScript.LAST_PRINT_BEATS.get(i), i);
				} else {
					break;
				}
			}
			echo.lastBeatIndex = beatIndex;

			// Periodic ambient dust (server particles — cheap, no entities)
			if (echo.tick % 8 == 0 && !ModConfig.INSTANCE.echoReducedParticles) {
				spawnDust(level, echo.center);
			}

			// Sync nearby players every 5 ticks + mid-join notify
			if (echo.tick % 5 == 0) {
				for (ServerPlayer player : nearby(level, echo.center)) {
					if (echo.notifiedPlayers.add(player.getUUID())) {
						EchoPayloads.sendStart(player, new EchoPayloads.EchoStartPayload(
							echo.id, echo.center, echo.durationTicks, echo.tick
						));
					}
					EchoPayloads.sendSync(player, new EchoPayloads.EchoSyncPayload(
						echo.id, echo.tick, echo.lastBeatIndex
					));
				}
			}

			if (echo.finished()) {
				finish(level, echo, true);
				it.remove();
			}
		}
	}

	private static void onBeat(ServerLevel level, ActiveEcho echo, EchoScript.Beat beat, int index) {
		BlockPos c = echo.center;
		// Subtitles to nearby players
		if (ModConfig.INSTANCE.echoSubtitles) {
			for (ServerPlayer player : nearby(level, c)) {
				player.sendOverlayMessage(Component.translatable(beat.subtitleKey()));
			}
		}
		// Lightweight world audio/particles (server)
		float vol = (float) Math.max(0.0, Math.min(1.0, ModConfig.INSTANCE.echoVolume));
		switch (beat.sfx()) {
			case "dust" -> spawnDust(level, c);
			case "footstep" -> level.playSound(null, c, SoundEvents.WOOD_STEP, SoundSource.AMBIENT, 0.4F * vol, 0.9F);
			case "press" -> level.playSound(null, c, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.AMBIENT, 0.5F * vol, 0.7F);
			case "impact" -> {
				if (!ModConfig.INSTANCE.echoReducedFlashes) {
					level.playSound(null, c, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.AMBIENT, 0.25F * vol, 1.4F);
				} else {
					level.playSound(null, c, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.AMBIENT, 0.4F * vol, 0.8F);
				}
			}
			case "hide" -> level.playSound(null, c, SoundEvents.CHEST_CLOSE, SoundSource.AMBIENT, 0.5F * vol, 1.1F);
			case "fade" -> level.playSound(null, c, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 0.3F * vol, 0.6F);
			case "clue" -> level.playSound(null, c, SoundEvents.BOOK_PAGE_TURN, SoundSource.AMBIENT, 0.6F * vol, 1.0F);
			default -> level.playSound(null, c, SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.2F * vol, 1.0F);
		}

		for (ServerPlayer player : nearby(level, c)) {
			EchoPayloads.sendSync(player, new EchoPayloads.EchoSyncPayload(echo.id, echo.tick, index));
		}
	}

	private static void finish(ServerLevel level, ActiveEcho echo, boolean completed) {
		for (ServerPlayer player : nearby(level, echo.center)) {
			EchoPayloads.sendEnd(player, new EchoPayloads.EchoEndPayload(echo.id, completed));
			if (completed && EchoScript.LAST_PRINT_RUN.equals(echo.id)) {
				ArchiveService.unlock(player, ArchiveEntries.ECHO_LAST_PRINT);
				ArchiveService.unlock(player, ArchiveEntries.CLUE_HIDDEN);
				player.sendSystemMessage(Component.translatable("echo.echoes_in_ink.last_print.complete"));
			}
		}
		ACTIVE.remove(echo);
	}

	private static void broadcastStart(ServerLevel level, ActiveEcho echo) {
		for (ServerPlayer player : nearby(level, echo.center)) {
			echo.notifiedPlayers.add(player.getUUID());
			EchoPayloads.sendStart(player, new EchoPayloads.EchoStartPayload(
				echo.id, echo.center, echo.durationTicks, 0
			));
		}
	}

	private static List<ServerPlayer> nearby(ServerLevel level, BlockPos center) {
		List<ServerPlayer> list = new ArrayList<>();
		Vec3 v = Vec3.atCenterOf(center);
		for (ServerPlayer player : PlayerLookup.around(level, v, VIEW_RANGE)) {
			list.add(player);
		}
		return list;
	}

	private static void spawnDust(ServerLevel level, BlockPos center) {
		if (ModConfig.INSTANCE.echoReducedParticles) {
			return;
		}
		double x = center.getX() + 0.5;
		double y = center.getY() + 1.0;
		double z = center.getZ() + 0.5;
		level.sendParticles(ParticleTypes.ASH, x, y, z, 6, 1.5, 0.6, 1.5, 0.01);
		level.sendParticles(ParticleTypes.WHITE_ASH, x, y + 0.4, z, 4, 1.2, 0.4, 1.2, 0.01);
	}
}
