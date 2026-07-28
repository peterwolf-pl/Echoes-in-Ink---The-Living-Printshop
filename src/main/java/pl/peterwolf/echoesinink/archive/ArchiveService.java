package pl.peterwolf.echoesinink.archive;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.sound.ModSounds;

/**
 * Server-side archive unlock + advancement helpers.
 */
public final class ArchiveService {
	private ArchiveService() {}

	public static PlayerArchive get(ServerPlayer player) {
		return ((AttachmentTarget) (Object) player).getAttachedOrCreate(ModAttachments.PLAYER_ARCHIVE, PlayerArchive::new);
	}

	public static void set(ServerPlayer player, PlayerArchive archive) {
		((AttachmentTarget) (Object) player).setAttached(ModAttachments.PLAYER_ARCHIVE, archive.copy());
	}

	/** Unlock an entry if new; notify player; optional advancement id path. */
	public static boolean unlock(ServerPlayer player, ArchiveEntries.Def entry) {
		return unlock(player, entry.id(), null);
	}

	public static boolean unlock(ServerPlayer player, String entryId) {
		return unlock(player, entryId, null);
	}

	public static boolean unlock(ServerPlayer player, String entryId, Identifier advancementId) {
		PlayerArchive archive = get(player);
		if (!archive.unlock(entryId)) {
			return false;
		}
		set(player, archive);
		player.sendOverlayMessage(Component.translatable("archive.echoes_in_ink.unlocked",
			Component.translatable("archive.echoes_in_ink.entry." + entryId + ".title")));
		player.level().playSound(null, player.blockPosition(), ModSounds.ARCHIVE_UNLOCK, SoundSource.PLAYERS, 0.35F, 1.2F);
		if (advancementId != null) {
			grantAdvancement(player, advancementId);
		}
		// Map common entries to advancements
		grantLinkedAdvancements(player, entryId, archive);
		return true;
	}

	public static void reset(ServerPlayer player) {
		PlayerArchive archive = get(player);
		archive.clear();
		set(player, archive);
	}

	public static void grantAdvancement(ServerPlayer player, Identifier id) {
		var server = player.level().getServer();
		if (server == null) {
			return;
		}
		AdvancementHolder holder = server.getAdvancements().get(id);
		if (holder == null) {
			EchoesInInk.LOGGER.debug("Missing advancement {}", id);
			return;
		}
		player.getAdvancements().award(holder, "unlocked");
	}

	private static void grantLinkedAdvancements(ServerPlayer player, String entryId, PlayerArchive archive) {
		// Dust and Ink — first investigation clue / debris
		if (entryId.equals(ArchiveEntries.CLUE_DUST.id()) || entryId.startsWith("part_") || entryId.equals(ArchiveEntries.MATRIX_TYPE.id())) {
			grantAdvancement(player, EchoesInInk.id("dust_and_ink"));
		}
		// Letters from the Rubble — type or page finds
		if (entryId.equals(ArchiveEntries.MATRIX_TYPE.id())
			|| entryId.equals(ArchiveEntries.CLUE_HIDDEN.id())
			|| entryId.equals(ArchiveEntries.WORK_CHRONICLE.id())) {
			grantAdvancement(player, EchoesInInk.id("letters_from_the_rubble"));
		}
		// Forgotten machine — any press part + workshop
		if (entryId.startsWith("part_") || entryId.equals(ArchiveEntries.WORKSHOP_ASHEN.id())) {
			if (archive.has(ArchiveEntries.PART_SCREW)
				|| archive.has(ArchiveEntries.PART_HANDLE)
				|| archive.has(ArchiveEntries.PART_PLATEN)
				|| archive.has(ArchiveEntries.PART_CARRIAGE)) {
				grantAdvancement(player, EchoesInInk.id("the_forgotten_machine"));
			}
		}
		if (entryId.equals(ArchiveEntries.WORK_INSTRUCTION.id())) {
			grantAdvancement(player, EchoesInInk.id("pull_the_handle"));
		}
		if (entryId.equals(ArchiveEntries.WORK_CHRONICLE.id())) {
			grantAdvancement(player, EchoesInInk.id("a_page_restored"));
		}
		if (entryId.equals(ArchiveEntries.ECHO_LAST_PRINT.id())) {
			grantAdvancement(player, EchoesInInk.id("echoes_in_ink"));
		}
		if (entryId.equals(ArchiveEntries.SITE_INK_CACHE.id())
			|| entryId.equals(ArchiveEntries.CLUE_CHRONICLE_MAP.id())) {
			grantAdvancement(player, EchoesInInk.id("the_buried_cache"));
		}
	}
}
