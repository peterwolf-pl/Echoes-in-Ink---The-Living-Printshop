package pl.peterwolf.echoesinink.structure;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import pl.peterwolf.echoesinink.config.ModConfig;
import pl.peterwolf.echoesinink.progression.WorkshopVariantSelector;

/**
 * Surface structure: a single abandoned print workshop with storage cellar.
 */
public class AbandonedPrintshopStructure extends Structure {
	public static final MapCodec<AbandonedPrintshopStructure> CODEC = simpleCodec(AbandonedPrintshopStructure::new);

	public AbandonedPrintshopStructure(StructureSettings settings) {
		super(settings);
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		if (!ModConfig.INSTANCE.enablePrintshopGeneration) {
			return Optional.empty();
		}
		return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, builder -> generatePieces(builder, context));
	}

	private void generatePieces(StructurePiecesBuilder builder, GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		WorldgenRandom random = context.random();
		int west = chunkPos.getMinBlockX() + 2;
		int north = chunkPos.getMinBlockZ() + 2;
		// Stable workshop id from chunk coords (for archive progression later).
		String workshopId = "printshop_" + Integer.toHexString(chunkPos.x() * 73856093 ^ chunkPos.z() * 19349663);
		WorkshopVariantSelector.Selection selection = WorkshopVariantSelector.select(
			workshopId,
			ModConfig.INSTANCE.enablePrintshopVariants
		);
		builder.addPiece(new AbandonedPrintshopPiece(
			random,
			west,
			north,
			workshopId,
			selection.variant(),
			selection.layoutIndex()
		));
	}

	@Override
	public StructureType<?> type() {
		return ModStructures.ABANDONED_PRINTSHOP_TYPE;
	}
}
