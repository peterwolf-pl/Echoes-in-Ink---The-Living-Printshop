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
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.block.ModBlocks;

/**
 * Compact ruined print workshop: main room + storage cellar + storytelling props.
 * Size fits one chunk comfortably to avoid generation stalls.
 */
public class AbandonedPrintshopPiece extends ScatteredFeaturePiece {
	/** Local size: width (X), height (Y), depth (Z). */
	private static final int WIDTH = 13;
	private static final int HEIGHT = 9;
	private static final int DEPTH = 11;

	public static final ResourceKey<LootTable> STORAGE_LOOT =
		ResourceKey.create(Registries.LOOT_TABLE, EchoesInInk.id("chests/printshop_storage"));
	public static final ResourceKey<LootTable> HIDDEN_LOOT =
		ResourceKey.create(Registries.LOOT_TABLE, EchoesInInk.id("chests/printshop_hidden"));

	private final String workshopId;

	public AbandonedPrintshopPiece(RandomSource random, int west, int north, String workshopId) {
		super(ModStructures.ABANDONED_PRINTSHOP_PIECE, west, 64, north, WIDTH, HEIGHT, DEPTH, getRandomHorizontalDirection(random));
		this.workshopId = workshopId == null || workshopId.isBlank() ? "printshop_unknown" : workshopId;
	}

	public AbandonedPrintshopPiece(CompoundTag tag) {
		super(ModStructures.ABANDONED_PRINTSHOP_PIECE, tag);
		this.workshopId = tag.getStringOr("WorkshopId", "printshop_unknown");
	}

	public String workshopId() {
		return workshopId;
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		super.addAdditionalSaveData(context, tag);
		tag.putString("WorkshopId", workshopId);
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
		// Floor sits on average ground; negative offset sinks foundation slightly.
		if (!this.updateAverageGroundHeight(level, chunkBB, -1)) {
			return;
		}

		// Clear interior volume (relative coords).
		this.generateAirBox(level, chunkBB, 0, 0, 0, WIDTH - 1, HEIGHT - 1, DEPTH - 1);

		// Foundation pillars under footprint (no floating).
		for (int x = 0; x < WIDTH; x++) {
			for (int z = 0; z < DEPTH; z++) {
				this.fillColumnDown(level, Blocks.COBBLESTONE.defaultBlockState(), x, -1, z, chunkBB);
			}
		}

		buildShell(level, chunkBB, random);
		buildMainWorkshop(level, chunkBB, random);
		buildStorageCellar(level, chunkBB, random);
		scatterStoryProps(level, chunkBB, random);
	}

	/** Outer walls, floor, partial roof — ruined look. */
	private void buildShell(WorldGenLevel level, BoundingBox box, RandomSource random) {
		BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
		BlockState logs = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
		BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
		BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
		BlockState floor = ModBlocks.INK_STAINED_FLOORBOARDS.defaultBlockState();
		BlockState dirtFloor = Blocks.COARSE_DIRT.defaultBlockState();

		// Floor: ink boards in workshop, dirt near door / collapse.
		for (int x = 1; x <= 11; x++) {
			for (int z = 1; z <= 9; z++) {
				boolean messy = (x + z) % 5 == 0 || random.nextFloat() < 0.08F;
				this.placeBlock(level, messy ? dirtFloor : floor, x, 0, z, box);
			}
		}

		// Walls of main room (x 0-8, z 0-9) and annex (x 8-12, z 0-6)
		// Outer perimeter
		for (int y = 1; y <= 4; y++) {
			for (int x = 0; x < WIDTH; x++) {
				placeWall(level, box, random, x, y, 0, planks, logs, cobble, mossy);
				if (x <= 8 || y <= 3) {
					placeWall(level, box, random, x, y, DEPTH - 1, planks, logs, cobble, mossy);
				}
			}
			for (int z = 0; z < DEPTH; z++) {
				placeWall(level, box, random, 0, y, z, planks, logs, cobble, mossy);
				if (z <= 6) {
					placeWall(level, box, random, WIDTH - 1, y, z, planks, logs, cobble, mossy);
				}
			}
		}

		// Internal wall between workshop and annex (partial, broken doorway)
		for (int y = 1; y <= 4; y++) {
			for (int z = 0; z <= 6; z++) {
				if (z == 3 || z == 4) {
					// open doorway
					this.placeBlock(level, Blocks.AIR.defaultBlockState(), 8, y, z, box);
				} else if (random.nextFloat() > 0.25F) {
					this.placeBlock(level, planks, 8, y, z, box);
				}
			}
		}

		// Blocked front doorway (work stopped suddenly — collapse)
		this.placeBlock(level, cobble, 4, 1, 0, box);
		this.placeBlock(level, mossy, 4, 2, 0, box);
		this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), 4, 3, 0, box);
		// Side entrance still usable
		this.placeBlock(level, Blocks.AIR.defaultBlockState(), 0, 1, 5, box);
		this.placeBlock(level, Blocks.AIR.defaultBlockState(), 0, 2, 5, box);

		// Partial roof — missing sections over press area
		for (int x = 0; x < WIDTH; x++) {
			for (int z = 0; z < DEPTH; z++) {
				boolean annexOnly = x > 8 && z > 6;
				if (annexOnly) {
					continue;
				}
				boolean collapsed = (x >= 3 && x <= 6 && z >= 4 && z <= 7) || random.nextFloat() < 0.12F;
				if (collapsed) {
					// open to sky — leaves / debris chance
					if (random.nextFloat() < 0.2F) {
						this.placeBlock(level, Blocks.OAK_LEAVES.defaultBlockState(), x, 5, z, box);
					}
				} else {
					this.placeBlock(level, Blocks.SPRUCE_SLAB.defaultBlockState(), x, 5, z, box);
					if (random.nextFloat() < 0.15F) {
						this.placeBlock(level, planks, x, 6, z, box);
					}
				}
			}
		}

		// Corner posts
		for (int y = 0; y <= 5; y++) {
			this.placeBlock(level, logs, 0, y, 0, box);
			this.placeBlock(level, logs, 8, y, 0, box);
			this.placeBlock(level, logs, 0, y, 9, box);
			this.placeBlock(level, logs, 8, y, 9, box);
		}
	}

	private void placeWall(
		WorldGenLevel level,
		BoundingBox box,
		RandomSource random,
		int x,
		int y,
		int z,
		BlockState planks,
		BlockState logs,
		BlockState cobble,
		BlockState mossy
	) {
		float r = random.nextFloat();
		if (r < 0.08F) {
			// missing wall section
			this.placeBlock(level, Blocks.AIR.defaultBlockState(), x, y, z, box);
		} else if (r < 0.18F) {
			this.placeBlock(level, mossy, x, y, z, box);
		} else if (r < 0.28F) {
			this.placeBlock(level, cobble, x, y, z, box);
		} else if (y == 1 && random.nextFloat() < 0.2F) {
			this.placeBlock(level, logs, x, y, z, box);
		} else {
			this.placeBlock(level, planks, x, y, z, box);
		}
	}

	/** Main print room props: press remains, tables, type, debris. */
	private void buildMainWorkshop(WorldGenLevel level, BoundingBox box, RandomSource random) {
		// Broken press centerpiece
		this.placeBlock(level, ModBlocks.BROKEN_PRESS_FRAME.defaultBlockState(), 4, 1, 5, box);
		this.placeBlock(level, ModBlocks.BROKEN_PRESS_FRAME.defaultBlockState(), 5, 1, 5, box);
		this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), 4, 2, 5, box); // remnant screw post
		this.placeBlock(level, Blocks.COBWEB.defaultBlockState(), 5, 2, 5, box);

		// Dusty printing table + chair pushed away (stairs facing out)
		this.placeBlock(level, ModBlocks.DUSTY_PRINTING_TABLE.defaultBlockState(), 2, 1, 3, box);
		BlockState chair = Blocks.OAK_STAIRS.defaultBlockState()
			.setValue(StairBlock.FACING, Direction.WEST)
			.setValue(StairBlock.HALF, Half.BOTTOM);
		this.placeBlock(level, chair, 1, 1, 2, box); // pushed back from desk

		// Unfinished matrix on table area
		this.placeBlock(level, ModBlocks.CARVED_WOODEN_MATRIX.defaultBlockState(), 2, 2, 3, box);

		// Type cabinet collapsed along wall
		this.placeBlock(level, ModBlocks.COLLAPSED_TYPE_CABINET.defaultBlockState(), 6, 1, 2, box);
		this.placeBlock(level, ModBlocks.COLLAPSED_TYPE_CABINET.defaultBlockState(), 7, 1, 2, box);

		// Archive shelf
		this.placeBlock(level, ModBlocks.DAMAGED_ARCHIVE_SHELF.defaultBlockState(), 1, 1, 7, box);
		this.placeBlock(level, ModBlocks.DAMAGED_ARCHIVE_SHELF.defaultBlockState(), 1, 2, 7, box);
		this.placeBlock(level, Blocks.BOOKSHELF.defaultBlockState(), 1, 3, 7, box);

		// Paper piles (white carpet) near press
		this.placeBlock(level, Blocks.CARPET.white().defaultBlockState(), 3, 1, 6, box);
		this.placeBlock(level, Blocks.CARPET.white().defaultBlockState(), 6, 1, 6, box);
		this.placeBlock(level, Blocks.CARPET.white().defaultBlockState(), 5, 1, 4, box);

		// Printing debris
		this.placeBlock(level, ModBlocks.PRINTING_DEBRIS.defaultBlockState(), 3, 1, 4, box);
		this.placeBlock(level, ModBlocks.PRINTING_DEBRIS.defaultBlockState(), 7, 1, 7, box);
		this.placeBlock(level, ModBlocks.PRINTING_DEBRIS.defaultBlockState(), 2, 1, 8, box);

		// Broken lantern on floor
		this.placeBlock(level, Blocks.LANTERN.defaultBlockState(), 5, 1, 7, box);

		// Cobwebs / dust feel
		this.placeBlock(level, Blocks.COBWEB.defaultBlockState(), 1, 3, 2, box);
		this.placeBlock(level, Blocks.COBWEB.defaultBlockState(), 7, 3, 8, box);
		this.placeBlock(level, Blocks.COBWEB.defaultBlockState(), 3, 4, 3, box);

		// Faded workshop plaque near side door
		this.placeBlock(level, ModBlocks.FADED_WORKSHOP_PLAQUE.defaultBlockState(), 1, 2, 5, box);

		// Storage chest in annex corner
		this.createChest(level, box, random, 10, 1, 2, STORAGE_LOOT);

		// Annex shelves / barrels
		this.placeBlock(level, Blocks.BARREL.defaultBlockState(), 10, 1, 5, box);
		this.placeBlock(level, Blocks.BARREL.defaultBlockState(), 11, 1, 5, box);
		this.placeBlock(level, ModBlocks.DAMAGED_ARCHIVE_SHELF.defaultBlockState(), 11, 1, 1, box);
		this.placeBlock(level, ModBlocks.PRINTING_DEBRIS.defaultBlockState(), 9, 1, 4, box);
	}

	/** Small cellar under annex — hidden compartment + ladder. */
	private void buildStorageCellar(WorldGenLevel level, BoundingBox box, RandomSource random) {
		// Hatch / ladder at annex floor (x=10, z=3)
		this.placeBlock(level, Blocks.AIR.defaultBlockState(), 10, 0, 3, box);
		BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
		for (int y = -3; y <= 0; y++) {
			this.placeBlock(level, ladder, 10, y, 3, box);
		}

		// Cellar volume under annex footprint
		for (int x = 8; x <= 11; x++) {
			for (int z = 1; z <= 5; z++) {
				for (int y = -3; y <= -1; y++) {
					if (x == 8 || x == 11 || z == 1 || z == 5 || y == -3) {
						this.placeBlock(level, Blocks.COBBLESTONE.defaultBlockState(), x, y, z, box);
					} else {
						this.placeBlock(level, Blocks.AIR.defaultBlockState(), x, y, z, box);
					}
				}
				// floor
				this.placeBlock(level, Blocks.COBBLESTONE.defaultBlockState(), x, -3, z, box);
			}
		}

		// Re-clear interior air
		this.generateAirBox(level, box, 9, -2, 2, 10, -1, 4);
		this.placeBlock(level, ladder, 10, -2, 3, box);
		this.placeBlock(level, ladder, 10, -1, 3, box);
		this.placeBlock(level, ladder, 10, 0, 3, box);

		// Hidden compartment chest (matrix / chronicle)
		this.createChest(level, box, random, 9, -2, 2, HIDDEN_LOOT);
		this.placeBlock(level, ModBlocks.CARVED_WOODEN_MATRIX.defaultBlockState(), 9, -2, 4, box);
		this.placeBlock(level, Blocks.COBWEB.defaultBlockState(), 10, -1, 2, box);
		this.placeBlock(level, Blocks.TORCH.defaultBlockState(), 11, -2, 3, box);
	}

	/** Extra environmental storytelling scatter. */
	private void scatterStoryProps(WorldGenLevel level, BoundingBox box, RandomSource random) {
		// Ink stain "puddles" as dark carpet
		this.placeBlock(level, Blocks.CARPET.black().defaultBlockState(), 4, 1, 6, box);
		this.placeBlock(level, Blocks.CARPET.black().defaultBlockState(), 5, 1, 6, box);

		// Flower pot / empty mug stand-in (cauldron empty? use flower pot)
		this.placeBlock(level, Blocks.FLOWER_POT.defaultBlockState(), 7, 1, 4, box);

		// More debris near blocked door
		this.placeBlock(level, ModBlocks.PRINTING_DEBRIS.defaultBlockState(), 4, 1, 1, box);
		if (random.nextBoolean()) {
			this.placeBlock(level, Blocks.COBWEB.defaultBlockState(), 4, 3, 1, box);
		}

		// Windows (air holes) with glass remnants
		this.placeBlock(level, Blocks.GLASS_PANE.defaultBlockState(), 3, 2, 9, box);
		this.placeBlock(level, Blocks.AIR.defaultBlockState(), 5, 2, 9, box); // broken window
		this.placeBlock(level, Blocks.GLASS_PANE.defaultBlockState(), 6, 2, 9, box);
	}
}
