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

/** Follow-up site: small buried ink archive cache. */
public class InkArchiveCacheStructure extends Structure {
	public static final MapCodec<InkArchiveCacheStructure> CODEC = simpleCodec(InkArchiveCacheStructure::new);

	public InkArchiveCacheStructure(StructureSettings settings) {
		super(settings);
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		if (!ModConfig.INSTANCE.enablePrintshopGeneration) {
			// Reuse generation toggle for secondary sites for now
			return Optional.empty();
		}
		return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, builder -> generatePieces(builder, context));
	}

	private void generatePieces(StructurePiecesBuilder builder, GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		WorldgenRandom random = context.random();
		int west = chunkPos.getMinBlockX() + 4;
		int north = chunkPos.getMinBlockZ() + 4;
		builder.addPiece(new InkArchiveCachePiece(random, west, north));
	}

	@Override
	public StructureType<?> type() {
		return ModStructures.INK_ARCHIVE_CACHE_TYPE;
	}
}
