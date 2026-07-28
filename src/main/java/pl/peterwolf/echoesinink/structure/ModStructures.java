package pl.peterwolf.echoesinink.structure;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import pl.peterwolf.echoesinink.EchoesInInk;

/**
 * Registers structure type + piece type. Placement/biomes come from datapack JSON.
 */
public final class ModStructures {
	public static final ResourceKey<Structure> ABANDONED_PRINTSHOP =
		ResourceKey.create(Registries.STRUCTURE, EchoesInInk.id("abandoned_printshop"));
	public static final ResourceKey<Structure> INK_ARCHIVE_CACHE =
		ResourceKey.create(Registries.STRUCTURE, EchoesInInk.id("ink_archive_cache"));

	public static StructureType<AbandonedPrintshopStructure> ABANDONED_PRINTSHOP_TYPE;
	public static StructurePieceType ABANDONED_PRINTSHOP_PIECE;
	public static StructureType<InkArchiveCacheStructure> INK_ARCHIVE_CACHE_TYPE;
	public static StructurePieceType INK_ARCHIVE_CACHE_PIECE;

	private ModStructures() {}

	public static void register() {
		ABANDONED_PRINTSHOP_TYPE = Registry.register(
			BuiltInRegistries.STRUCTURE_TYPE,
			EchoesInInk.id("abandoned_printshop"),
			() -> AbandonedPrintshopStructure.CODEC
		);
		ABANDONED_PRINTSHOP_PIECE = Registry.register(
			BuiltInRegistries.STRUCTURE_PIECE,
			EchoesInInk.id("abandoned_printshop_piece"),
			(StructurePieceType.ContextlessType) AbandonedPrintshopPiece::new
		);

		INK_ARCHIVE_CACHE_TYPE = Registry.register(
			BuiltInRegistries.STRUCTURE_TYPE,
			EchoesInInk.id("ink_archive_cache"),
			() -> InkArchiveCacheStructure.CODEC
		);
		INK_ARCHIVE_CACHE_PIECE = Registry.register(
			BuiltInRegistries.STRUCTURE_PIECE,
			EchoesInInk.id("ink_archive_cache_piece"),
			(StructurePieceType.ContextlessType) InkArchiveCachePiece::new
		);

		EchoesInInk.LOGGER.info("Registered printshop + ink archive cache structure types.");
	}
}
