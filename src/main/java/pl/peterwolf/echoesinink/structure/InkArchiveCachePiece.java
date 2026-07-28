package pl.peterwolf.echoesinink.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.block.ModBlocks;

/**
 * Compact half-buried archive cache: hatch, ladder, chamber, chest of matrices.
 */
public class InkArchiveCachePiece extends ScatteredFeaturePiece {
	private static final int WIDTH = 9;
	private static final int HEIGHT = 8;
	private static final int DEPTH = 9;

	public static final ResourceKey<LootTable> CACHE_LOOT =
		ResourceKey.create(Registries.LOOT_TABLE, EchoesInInk.id("chests/ink_archive_cache"));

	public InkArchiveCachePiece(RandomSource random, int west, int north) {
		super(ModStructures.INK_ARCHIVE_CACHE_PIECE, west, 64, north, WIDTH, HEIGHT, DEPTH, getRandomHorizontalDirection(random));
	}

	public InkArchiveCachePiece(CompoundTag tag) {
		super(ModStructures.INK_ARCHIVE_CACHE_PIECE, tag);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		super.addAdditionalSaveData(context, tag);
	}

	@Override
	public void postProcess(
		WorldGenLevel level,
		StructureManager structureManager,
		ChunkGenerator generator,
		RandomSource random,
		BoundingBox chunkBB,
		ChunkPos chunkPos,
		BlockPos referencePos
	) {
		// Sink so most of the chamber is underground (avoid exposed underground rooms).
		if (!this.updateAverageGroundHeight(level, chunkBB, -4)) {
			return;
		}

		this.generateAirBox(level, chunkBB, 0, 0, 0, WIDTH - 1, HEIGHT - 1, DEPTH - 1);

		BlockState stone = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
		BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
		BlockState dirt = Blocks.DIRT.defaultBlockState();

		// Foundation fill under chamber
		for (int x = 0; x < WIDTH; x++) {
			for (int z = 0; z < DEPTH; z++) {
				this.fillColumnDown(level, cobble, x, -1, z, chunkBB);
			}
		}

		// Chamber shell (buried)
		for (int y = 0; y <= 4; y++) {
			for (int x = 0; x < WIDTH; x++) {
				for (int z = 0; z < DEPTH; z++) {
					boolean wall = x == 0 || z == 0 || x == WIDTH - 1 || z == DEPTH - 1 || y == 0 || y == 4;
					if (wall) {
						this.placeBlock(level, random.nextFloat() < 0.2F ? cobble : stone, x, y, z, chunkBB);
					} else {
						this.placeBlock(level, Blocks.AIR.defaultBlockState(), x, y, z, chunkBB);
					}
				}
			}
		}

		// Dirt cap on top so surface looks natural
		for (int x = 1; x < WIDTH - 1; x++) {
			for (int z = 1; z < DEPTH - 1; z++) {
				this.placeBlock(level, dirt, x, 5, z, chunkBB);
				if (random.nextFloat() < 0.3F) {
					this.placeBlock(level, Blocks.GRASS_BLOCK.defaultBlockState(), x, 6, z, chunkBB);
				}
			}
		}

		// Hatch / mossy trapdoor look using oak trapdoor if available
		this.placeBlock(level, Blocks.AIR.defaultBlockState(), 4, 4, 4, chunkBB);
		this.placeBlock(level, Blocks.AIR.defaultBlockState(), 4, 5, 4, chunkBB);
		this.placeBlock(level, Blocks.AIR.defaultBlockState(), 4, 6, 4, chunkBB);
		BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
		for (int y = 1; y <= 6; y++) {
			this.placeBlock(level, ladder, 4, y, 4, chunkBB);
		}
		// Subtle surface marker (moss carpet)
		this.placeBlock(level, Blocks.MOSS_CARPET.defaultBlockState(), 4, 7, 4, chunkBB);

		// Interior: shelves, matrices, chest
		this.placeBlock(level, ModBlocks.DAMAGED_ARCHIVE_SHELF.defaultBlockState(), 2, 1, 2, chunkBB);
		this.placeBlock(level, ModBlocks.DAMAGED_ARCHIVE_SHELF.defaultBlockState(), 2, 2, 2, chunkBB);
		this.placeBlock(level, ModBlocks.CARVED_WOODEN_MATRIX.defaultBlockState(), 6, 1, 2, chunkBB);
		this.placeBlock(level, ModBlocks.PRINTING_DEBRIS.defaultBlockState(), 6, 1, 6, chunkBB);
		this.placeBlock(level, ModBlocks.COLLAPSED_TYPE_CABINET.defaultBlockState(), 2, 1, 6, chunkBB);
		this.placeBlock(level, Blocks.COBWEB.defaultBlockState(), 3, 3, 3, chunkBB);
		this.placeBlock(level, Blocks.COBWEB.defaultBlockState(), 5, 3, 5, chunkBB);
		this.placeBlock(level, Blocks.TORCH.defaultBlockState(), 7, 2, 4, chunkBB);

		this.createChest(level, chunkBB, random, 5, 1, 5, CACHE_LOOT);
		// Second "crate" barrel
		this.placeBlock(level, Blocks.BARREL.defaultBlockState(), 3, 1, 5, chunkBB);
	}
}
