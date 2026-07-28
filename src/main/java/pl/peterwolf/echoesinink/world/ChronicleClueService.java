package pl.peterwolf.echoesinink.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import pl.peterwolf.echoesinink.archive.ArchiveEntries;
import pl.peterwolf.echoesinink.archive.ArchiveService;
import pl.peterwolf.echoesinink.archive.PlayerArchive;
import pl.peterwolf.echoesinink.sound.ModSounds;
import pl.peterwolf.echoesinink.structure.ModStructures;

/**
 * Progressive chronicle reading: biome → bearing → map → precise site.
 * Server-only; never exposes raw coords until late stage (accessibility).
 */
public final class ChronicleClueService {
	private ChronicleClueService() {}

	/**
	 * @return true if the chronicle item was used productively
	 */
	public static boolean read(ServerPlayer player, ItemStack chronicle) {
		ServerLevel level = player.level();
		PlayerArchive archive = ArchiveService.get(player);

		// Must have been legitimately restored (archive entry) OR allow if they hold the page
		// after printing — printing already unlocks WORK_CHRONICLE.
		if (!archive.has(ArchiveEntries.WORK_CHRONICLE) && !archive.has(ArchiveEntries.CLUE_DUST)) {
			player.sendOverlayMessage(Component.translatable("chronicle.echoes_in_ink.need_investigation"));
			return false;
		}

		boolean progressed = readStages(player, level, archive);
		if (progressed) {
			level.playSound(null, player.blockPosition(), ModSounds.CHRONICLE_READ, SoundSource.PLAYERS, 0.7F, 1.05F);
		}
		return progressed;
	}

	private static boolean readStages(ServerPlayer player, ServerLevel level, PlayerArchive archive) {

		// Stage 1 — biome
		if (!archive.has(ArchiveEntries.CLUE_CHRONICLE_BIOME)) {
			if (!archive.has(ArchiveEntries.CLUE_DUST) && !archive.has(ArchiveEntries.CLUE_HIDDEN)) {
				player.sendOverlayMessage(Component.translatable("chronicle.echoes_in_ink.need_more_investigation"));
				return false;
			}
			ArchiveService.unlock(player, ArchiveEntries.CLUE_CHRONICLE_BIOME);
			player.sendSystemMessage(Component.translatable("chronicle.echoes_in_ink.stage.biome"));
			player.sendOverlayMessage(Component.translatable("chronicle.echoes_in_ink.stage.biome.short"));
			return true;
		}

		// Stage 2 — direction
		if (!archive.has(ArchiveEntries.CLUE_CHRONICLE_BEARING)) {
			if (!archive.has(ArchiveEntries.ECHO_LAST_PRINT) && !archive.has(ArchiveEntries.CLUE_HIDDEN)) {
				player.sendOverlayMessage(Component.translatable("chronicle.echoes_in_ink.need_echo_or_hidden"));
				return false;
			}
			ArchiveService.unlock(player, ArchiveEntries.CLUE_CHRONICLE_BEARING);
			BlockPos target = findNearestCache(level, player.blockPosition());
			if (target != null) {
				String bearing = bearingFrom(player.blockPosition(), target);
				player.sendSystemMessage(Component.translatable("chronicle.echoes_in_ink.stage.bearing", bearing));
				player.sendOverlayMessage(Component.translatable("chronicle.echoes_in_ink.stage.bearing.short", bearing));
			} else {
				player.sendSystemMessage(Component.translatable("chronicle.echoes_in_ink.stage.bearing.unknown"));
			}
			return true;
		}

		// Stage 3 — map
		if (!archive.has(ArchiveEntries.CLUE_CHRONICLE_MAP)) {
			// Prefer printed map fragment as story prereq; allow after bearing alone for accessibility
			if (!archive.has(ArchiveEntries.WORK_MAP) && !archive.has(ArchiveEntries.CLUE_CHRONICLE_BEARING)) {
				player.sendOverlayMessage(Component.translatable("chronicle.echoes_in_ink.need_map_fragment"));
				return false;
			}
			BlockPos target = findNearestCache(level, player.blockPosition());
			if (target == null) {
				player.sendSystemMessage(Component.translatable("chronicle.echoes_in_ink.no_site"));
				return false;
			}
			ItemStack map = createCacheMap(level, target);
			if (!player.getInventory().add(map)) {
				player.drop(map, false);
			}
			ArchiveService.unlock(player, ArchiveEntries.CLUE_CHRONICLE_MAP);
			ArchiveService.unlock(player, ArchiveEntries.WORK_MAP);
			player.sendSystemMessage(Component.translatable("chronicle.echoes_in_ink.stage.map"));
			player.sendOverlayMessage(Component.translatable("chronicle.echoes_in_ink.stage.map.short"));
			return true;
		}

		// Stage 4 — precise location (accessibility + completion)
		if (!archive.has(ArchiveEntries.SITE_INK_CACHE)) {
			BlockPos target = findNearestCache(level, player.blockPosition());
			ArchiveService.unlock(player, ArchiveEntries.SITE_INK_CACHE);
			if (target != null) {
				int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getX(), target.getZ());
				player.sendSystemMessage(Component.translatable(
					"chronicle.echoes_in_ink.stage.precise",
					target.getX(), y, target.getZ()
				));
				player.sendOverlayMessage(Component.translatable("chronicle.echoes_in_ink.stage.precise.short"));
			} else {
				player.sendSystemMessage(Component.translatable("chronicle.echoes_in_ink.no_site"));
			}
			return true;
		}

		// Already fully decoded — restate
		BlockPos target = findNearestCache(level, player.blockPosition());
		if (target != null) {
			int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getX(), target.getZ());
			player.sendSystemMessage(Component.translatable(
				"chronicle.echoes_in_ink.stage.precise",
				target.getX(), y, target.getZ()
			));
		} else {
			player.sendSystemMessage(Component.translatable("chronicle.echoes_in_ink.complete_no_site"));
		}
		return true;
	}

	public static BlockPos findNearestCache(ServerLevel level, BlockPos origin) {
		var structure = level.registryAccess()
			.lookupOrThrow(Registries.STRUCTURE)
			.get(ModStructures.INK_ARCHIVE_CACHE);
		if (structure.isEmpty()) {
			return null;
		}
		var result = level.getChunkSource().getGenerator()
			.findNearestMapStructure(level, HolderSet.direct(structure.get()), origin, 100, false);
		return result == null ? null : result.getFirst();
	}

	public static ItemStack createCacheMap(ServerLevel level, BlockPos target) {
		ItemStack map = MapItem.create(level, target.getX(), target.getZ(), (byte) 2, true, true);
		MapItem.renderBiomePreviewMap(level, map);
		MapItemSavedData.addTargetDecoration(map, target, "+", MapDecorationTypes.RED_X);
		map.set(net.minecraft.core.component.DataComponents.ITEM_NAME,
			Component.translatable("item.echoes_in_ink.ink_cache_map"));
		return map;
	}

	private static String bearingFrom(BlockPos from, BlockPos to) {
		double dx = to.getX() - from.getX();
		double dz = to.getZ() - from.getZ();
		double angle = Math.toDegrees(Math.atan2(dz, dx));
		// Minecraft: +Z south, +X east. atan2(dz,dx): 0 = east
		String[] names = {"E", "SE", "S", "SW", "W", "NW", "N", "NE"};
		int idx = (int) Math.round(((angle % 360) + 360) % 360 / 45.0) % 8;
		return names[idx];
	}
}
