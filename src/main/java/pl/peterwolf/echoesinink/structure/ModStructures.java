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

	public static StructureType<AbandonedPrintshopStructure> ABANDONED_PRINTSHOP_TYPE;
	public static StructurePieceType ABANDONED_PRINTSHOP_PIECE;

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

		EchoesInInk.LOGGER.info("Registered abandoned printshop structure type.");
	}
}
