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
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import pl.peterwolf.echoesinink.config.ModConfig;
import pl.peterwolf.echoesinink.progression.WorkshopIdentity;
import pl.peterwolf.echoesinink.progression.WorkshopVariantSelector;

/**
 * Surface printshop. Village-grid chunks in village biomes sit on the
 * outskirts (two shops at larger villages). Other placements are scattered
 * wilderness workshops.
 */
public class AbandonedPrintshopStructure extends Structure {
	public static final MapCodec<AbandonedPrintshopStructure> CODEC = simpleCodec(AbandonedPrintshopStructure::new);
	private static final RandomSpreadStructurePlacement VILLAGE_GRID = new RandomSpreadStructurePlacement(
		VillagePrintshopLayout.VILLAGE_SPACING,
		VillagePrintshopLayout.VILLAGE_SEPARATION,
		RandomSpreadType.LINEAR,
		VillagePrintshopLayout.VILLAGE_SALT
	);

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
		String biome = biomePath(context);
		if (VillagePrintshopLayout.isVillageBiome(biome) && isVillageGridChunk(context)) {
			addVillagePrintshops(builder, context, random, chunkPos, biome);
			return;
		}
		addWildernessPrintshop(builder, random, chunkPos);
	}

	private static void addVillagePrintshops(
		StructurePiecesBuilder builder,
		GenerationContext context,
		WorldgenRandom random,
		ChunkPos chunkPos,
		String biome
	) {
		int count = VillagePrintshopLayout.printshopCount(biome);
		int centerX = chunkPos.getMiddleBlockX();
		int centerZ = chunkPos.getMiddleBlockZ();
		long seed = context.seed() ^ chunkPos.pack();
		for (int index = 0; index < count; index++) {
			int[] offset = VillagePrintshopLayout.originOffset(index, seed);
			addPrintshop(builder, random, chunkPos, index, centerX + offset[0], centerZ + offset[1]);
		}
	}

	private static void addWildernessPrintshop(
		StructurePiecesBuilder builder,
		WorldgenRandom random,
		ChunkPos chunkPos
	) {
		addPrintshop(
			builder,
			random,
			chunkPos,
			0,
			chunkPos.getMinBlockX() + 2,
			chunkPos.getMinBlockZ() + 2
		);
	}

	private static void addPrintshop(
		StructurePiecesBuilder builder,
		WorldgenRandom random,
		ChunkPos chunkPos,
		int index,
		int west,
		int north
	) {
		String workshopId = WorkshopIdentity.idForChunk(chunkPos, index);
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

	private static boolean isVillageGridChunk(GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		ChunkPos villageChunk = VILLAGE_GRID.getPotentialStructureChunk(
			context.seed(),
			chunkPos.x(),
			chunkPos.z()
		);
		return villageChunk.x() == chunkPos.x() && villageChunk.z() == chunkPos.z();
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
