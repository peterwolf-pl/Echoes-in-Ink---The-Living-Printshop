package pl.peterwolf.echoesinink.structure;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import pl.peterwolf.echoesinink.config.ModConfig;
import pl.peterwolf.echoesinink.progression.WorkshopIdentity;
import pl.peterwolf.echoesinink.progression.WorkshopVariantSelector;

/**
 * Surface structure locked to the vanilla village grid. One workshop sits on
 * the village edge; larger village biomes also get a second workshop opposite.
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
		int count = VillagePrintshopLayout.printshopCount(biomePath(context));
		int centerX = chunkPos.getMiddleBlockX();
		int centerZ = chunkPos.getMiddleBlockZ();
		long seed = context.seed() ^ chunkPos.pack();
		for (int index = 0; index < count; index++) {
			int[] offset = VillagePrintshopLayout.originOffset(index, seed);
			String workshopId = WorkshopIdentity.idForChunk(chunkPos, index);
			WorkshopVariantSelector.Selection selection = WorkshopVariantSelector.select(
				workshopId,
				ModConfig.INSTANCE.enablePrintshopVariants
			);
			builder.addPiece(new AbandonedPrintshopPiece(
				random,
				centerX + offset[0],
				centerZ + offset[1],
				workshopId,
				selection.variant(),
				selection.layoutIndex()
			));
		}
	}

	private static String biomePath(GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		Holder<Biome> biome = context.biomeSource().getNoiseBiome(
			QuartPos.fromBlock(chunkPos.getMiddleBlockX()),
			QuartPos.fromBlock(64),
			QuartPos.fromBlock(chunkPos.getMiddleBlockZ()),
			context.randomState().sampler()
		);
		return biome.unwrapKey().map(key -> key.identifier().getPath()).orElse("");
	}

	@Override
	public StructureType<?> type() {
		return ModStructures.ABANDONED_PRINTSHOP_TYPE;
	}
}
