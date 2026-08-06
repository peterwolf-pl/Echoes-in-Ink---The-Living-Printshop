package pl.peterwolf.echoesinink.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import pl.peterwolf.echoesinink.block.FadedWorkshopPlaqueBlock;
import pl.peterwolf.echoesinink.block.HangingPosterBlock;
import pl.peterwolf.echoesinink.block.LaidPaperBlock;
import pl.peterwolf.echoesinink.block.entity.InvestigationBlockEntity;
import pl.peterwolf.echoesinink.block.entity.LaidPaperBlockEntity;
import pl.peterwolf.echoesinink.config.ModConfig;
import pl.peterwolf.echoesinink.item.ModItems;
import pl.peterwolf.echoesinink.progression.InvestigationRole;
import pl.peterwolf.echoesinink.progression.WorkshopIdentity;
import pl.peterwolf.echoesinink.progression.WorkshopLayoutPlan;
import pl.peterwolf.echoesinink.progression.WorkshopVariant;

/**
 * Four stable procedural printshop types, each with two layout subvariants.
 * Mandatory progression is attached to semantic investigation nodes rather
 * than chests, so every major type can safely become the world's starter.
 */
public class AbandonedPrintshopPiece extends ScatteredFeaturePiece {
	private static final int WIDTH = 17;
	private static final int HEIGHT = 10;
	private static final int DEPTH = 15;

	public static final ResourceKey<LootTable> STORAGE_LOOT = loot("chests/printshop_storage");
	public static final ResourceKey<LootTable> HIDDEN_LOOT = loot("chests/printshop_hidden");
	public static final ResourceKey<LootTable> RURAL_LOOT = loot("chests/printshop_rural");
	public static final ResourceKey<LootTable> TOWN_LOOT = loot("chests/printshop_town");
	public static final ResourceKey<LootTable> SCHOLARLY_LOOT = loot("chests/printshop_scholarly");
	public static final ResourceKey<LootTable> BURNED_LOOT = loot("chests/printshop_burned");

	private final String workshopId;
	private final WorkshopVariant variant;
	private final int layoutIndex;

	public AbandonedPrintshopPiece(RandomSource random, int west, int north, String workshopId) {
		this(random, west, north, workshopId, WorkshopVariant.RURAL_WOODCUT, 0);
	}

	public AbandonedPrintshopPiece(
		RandomSource random,
		int west,
		int north,
		String workshopId,
		WorkshopVariant variant,
		int layoutIndex
	) {
		super(
			ModStructures.ABANDONED_PRINTSHOP_PIECE,
			west,
			64,
			north,
			WIDTH,
			HEIGHT,
			DEPTH,
			getRandomHorizontalDirection(random)
		);
		WorkshopIdentity identity = new WorkshopIdentity(workshopId, variant, layoutIndex);
		this.workshopId = identity.workshopId();
		this.variant = identity.variant();
		this.layoutIndex = identity.layoutIndex();
	}

	public AbandonedPrintshopPiece(CompoundTag tag) {
		super(ModStructures.ABANDONED_PRINTSHOP_PIECE, tag);
		WorkshopIdentity identity = WorkshopIdentity.fromStored(
			tag.getStringOr("WorkshopId", "printshop_unknown"),
			tag.getStringOr("WorkshopVariant", WorkshopVariant.RURAL_WOODCUT.id()),
			tag.getIntOr("LayoutIndex", 0)
		);
		workshopId = identity.workshopId();
		variant = identity.variant();
		layoutIndex = identity.layoutIndex();
	}

	public String workshopId() {
		return workshopId;
	}

	public WorkshopVariant variant() {
		return variant;
	}

	public int layoutIndex() {
		return layoutIndex;
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		super.addAdditionalSaveData(context, tag);
		tag.putString("WorkshopId", workshopId);
		tag.putString("WorkshopVariant", variant.id());
		tag.putInt("LayoutIndex", layoutIndex);
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
		if (!updateAverageGroundHeight(level, chunkBB, -1)) {
			return;
		}

		switch (variant) {
			case RURAL_WOODCUT -> buildRural(level, chunkBB, random);
			case TOWN_TYPE_FOUNDRY -> buildTown(level, chunkBB, random);
			case SCHOLARLY_ARCHIVE -> buildScholarly(level, chunkBB, random);
			case BURNED_CLANDESTINE -> buildBurned(level, chunkBB, random);
		}
	}

	private void buildRural(WorldGenLevel level, BoundingBox box, RandomSource random) {
		int width = 13;
		int depth = 11;
		prepareSite(level, box, width, depth, Blocks.COBBLESTONE.defaultBlockState());
		fillDecorativeFloor(level, box, width, depth, Blocks.COARSE_DIRT.defaultBlockState(), 7);
		buildPerimeter(level, box, width, depth, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 3);
		int doorZ = layoutIndex == 0 ? 5 : 7;
		carveDoor(level, box, 0, doorZ);
		buildRoof(level, box, width, depth, Blocks.SPRUCE_SLAB.defaultBlockState(), 5, 4, 6, 2);

		// Open carving annex swaps side between layout subvariants.
		int annexWall = mx(8, width);
		for (int z = 1; z <= 6; z++) {
			for (int y = 1; y <= 4; y++) {
				if (z != 3 && z != 4) {
					placeBlock(level, Blocks.OAK_PLANKS.defaultBlockState(), annexWall, y, z, box);
				}
			}
		}

		placeNode(level, box, ModBlocks.PRESS_FRAME.defaultBlockState(), mx(4, width), 1, 5, InvestigationRole.PRESS_FRAME);
		placeNode(level, box, ModBlocks.PRESS_FRAME.defaultBlockState(), mx(5, width), 1, 5, InvestigationRole.MACHINE_REMAINS);
		placeNode(level, box, ModBlocks.DUSTY_PRINTING_TABLE.defaultBlockState(), mx(2, width), 1, 3, InvestigationRole.MATRIX_BENCH);
		placeNode(level, box, ModBlocks.DAMAGED_ARCHIVE_SHELF.defaultBlockState(), mx(1, width), 1, 8, InvestigationRole.ARCHIVE_DESK);
		placeNode(level, box, ModBlocks.COLLAPSED_TYPE_CABINET.defaultBlockState(), mx(7, width), 1, 2, InvestigationRole.INK_STATION);
		placeNode(level, box, ModBlocks.FADED_WORKSHOP_PLAQUE.defaultBlockState()
			.setValue(FadedWorkshopPlaqueBlock.FACING, layoutIndex == 0 ? Direction.EAST : Direction.WEST),
			mx(1, width), 2, layoutIndex == 0 ? doorZ + 1 : doorZ - 1, InvestigationRole.PLAQUE_CLUE);
		placeNode(level, box, ModBlocks.HIDDEN_FLOOR_COMPARTMENT.defaultBlockState(), mx(9, width), 0, 4, InvestigationRole.FLOOR_CACHE);

		buildCellar(level, box, mx(10, width), 3, mx(8, width), mx(11, width), 1, 5, Blocks.COBBLESTONE.defaultBlockState(), mx(9, width), 4);
		placeNode(level, box, ModBlocks.PRINTING_DEBRIS.defaultBlockState(), mx(9, width), -2, 4, InvestigationRole.CELLAR_CACHE);
		placeSuspiciousFloors(level, box, width, new int[][] {{3, 2}, {7, 8}, {2, 6}, {10, 6}});

		placeBlock(level, Blocks.LOOM.defaultBlockState(), mx(10, width), 1, 2, box);
		placeBlock(level, Blocks.BARREL.defaultBlockState(), mx(11, width), 1, 5, box);
		placeBlock(level, Blocks.HAY_BLOCK.defaultBlockState(), mx(10, width), 1, 8, box);
		placeBlock(level, Blocks.CARPET.white().defaultBlockState(), mx(3, width), 1, 6, box);
		placeBlock(level, ModBlocks.CARVED_WOODEN_MATRIX.defaultBlockState(), mx(2, width), 2, 3, box);
		placeBlock(level, Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.HALF, Half.BOTTOM), mx(1, width), 1, 2, box);
		placeBlock(level, Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), mx(6, width), 1, 8, box);
		placeLaidPrint(level, box, mx(6, width), 2, 8, Direction.EAST, ModItems.DECORATIVE_WOODCUT);
		placeLaidPrint(level, box, mx(11, width), 2, 5, Direction.NORTH, ModItems.DAMAGED_ARCHIVE_PAGE);
		placeHangingPrint(level, box, mx(6, width), 2, 9, Direction.NORTH);
		placeHangingPrint(level, box, mx(11, width), 2, 1, Direction.SOUTH);
		placeCobwebs(level, box, new int[][] {{1, 4, 2}, {5, 2, 5}, {7, 3, 8}, {10, 4, 9}});
		createChest(level, box, random, mx(10, width), 1, 7, RURAL_LOOT);
	}

	private void buildTown(WorldGenLevel level, BoundingBox box, RandomSource random) {
		int width = 17;
		int depth = 13;
		prepareSite(level, box, width, depth, Blocks.STONE_BRICKS.defaultBlockState());
		fillDecorativeFloor(level, box, width, depth, Blocks.ANDESITE.defaultBlockState(), 11);
		buildPerimeter(level, box, width, depth, Blocks.BRICKS.defaultBlockState(), Blocks.POLISHED_ANDESITE.defaultBlockState(), 5);
		int entranceX = layoutIndex == 0 ? 8 : 5;
		carveDoor(level, box, entranceX, 0);
		buildRoof(level, box, width, depth, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 9, 11, 5, 2);

		// Heavy foundry wing and a public composing room are divided differently.
		int dividerZ = layoutIndex == 0 ? 4 : 8;
		for (int x = 1; x < width - 1; x++) {
			if (x != entranceX && x != entranceX + 1) {
				placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x, 3, dividerZ, box);
			}
		}

		placeNode(level, box, ModBlocks.PRESS_FRAME.defaultBlockState(), mx(7, width), 1, 7, InvestigationRole.PRESS_FRAME);
		placeNode(level, box, ModBlocks.PRINTING_DEBRIS.defaultBlockState(), mx(13, width), 1, 5, InvestigationRole.MACHINE_REMAINS);
		placeNode(level, box, ModBlocks.DUSTY_PRINTING_TABLE.defaultBlockState(), mx(5, width), 1, 3, InvestigationRole.MATRIX_BENCH);
		placeNode(level, box, ModBlocks.DAMAGED_ARCHIVE_SHELF.defaultBlockState(), mx(2, width), 1, 10, InvestigationRole.ARCHIVE_DESK);
		placeNode(level, box, ModBlocks.COLLAPSED_TYPE_CABINET.defaultBlockState(), mx(11, width), 1, 3, InvestigationRole.INK_STATION);
		placeNode(level, box, ModBlocks.FADED_WORKSHOP_PLAQUE.defaultBlockState()
			.setValue(FadedWorkshopPlaqueBlock.FACING, Direction.NORTH), entranceX + 1, 2, 1, InvestigationRole.PLAQUE_CLUE);
		placeNode(level, box, ModBlocks.HIDDEN_FLOOR_COMPARTMENT.defaultBlockState(), mx(14, width), 0, 9, InvestigationRole.FLOOR_CACHE);

		buildCellar(level, box, mx(14, width), 10, mx(12, width), mx(15, width), 8, 11, Blocks.STONE_BRICKS.defaultBlockState(), mx(13, width), 9);
		placeNode(level, box, ModBlocks.COLLAPSED_TYPE_CABINET.defaultBlockState(), mx(13, width), -2, 9, InvestigationRole.CELLAR_CACHE);
		placeSuspiciousFloors(level, box, width, new int[][] {{3, 5}, {8, 10}, {12, 2}, {14, 6}});

		for (int x : new int[] {2, 4, 12, 14}) {
			placeBlock(level, ModBlocks.COLLAPSED_TYPE_CABINET.defaultBlockState(), mx(x, width), 1, 2, box);
		}
		placeBlock(level, Blocks.BLAST_FURNACE.defaultBlockState(), mx(14, width), 1, 5, box);
		placeBlock(level, Blocks.ANVIL.defaultBlockState(), mx(12, width), 1, 6, box);
		placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), mx(15, width), 2, 5, box);
		placeBlock(level, Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), mx(3, width), 1, 9, box);
		placeBlock(level, Blocks.SMITHING_TABLE.defaultBlockState(), mx(10, width), 1, 9, box);
		placeLaidPrint(level, box, mx(3, width), 2, 9, Direction.WEST, ModItems.DECORATIVE_WOODCUT);
		placeLaidPrint(level, box, mx(10, width), 2, 9, Direction.SOUTH, ModItems.BLANK_ARCHIVE_PAGE);
		placeHangingPrint(level, box, entranceX + 2, 2, 1, Direction.SOUTH);
		placeHangingPrint(level, box, mx(2, width), 3, 1, Direction.SOUTH);
		placeHangingPrint(level, box, mx(15, width), 2, 6, layoutIndex == 0 ? Direction.WEST : Direction.EAST);
		placeCobwebs(level, box, new int[][] {{1, 4, 1}, {7, 4, 10}, {15, 4, 11}, {12, 3, 2}});
		createChest(level, box, random, mx(14, width), 1, 10, TOWN_LOOT);
	}

	private void buildScholarly(WorldGenLevel level, BoundingBox box, RandomSource random) {
		int width = 15;
		int depth = 15;
		prepareSite(level, box, width, depth, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
		fillDecorativeFloor(level, box, width, depth, Blocks.POLISHED_ANDESITE.defaultBlockState(), 13);
		buildPerimeter(level, box, width, depth, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.DARK_OAK_LOG.defaultBlockState(), 7);
		int entranceZ = layoutIndex == 0 ? 7 : 4;
		carveDoor(level, box, width - 1, entranceZ);
		buildRoof(level, box, width, depth, Blocks.DARK_OAK_SLAB.defaultBlockState(), 15, 5, 10, 3);

		// L-shaped catalog wing around a small reading court.
		int catalogZ = layoutIndex == 0 ? 11 : 3;
		for (int x = 1; x <= 6; x++) {
			for (int y = 1; y <= 4; y++) {
				if (x != 4 || y > 2) {
					placeBlock(level, Blocks.DARK_OAK_PLANKS.defaultBlockState(), x, y, catalogZ, box);
				}
			}
		}

		placeNode(level, box, ModBlocks.PRESS_FRAME.defaultBlockState(), 7, 1, mz(8, depth), InvestigationRole.PRESS_FRAME);
		placeNode(level, box, ModBlocks.PRINTING_DEBRIS.defaultBlockState(), 10, 1, mz(10, depth), InvestigationRole.MACHINE_REMAINS);
		placeNode(level, box, ModBlocks.DUSTY_PRINTING_TABLE.defaultBlockState(), 9, 1, mz(5, depth), InvestigationRole.MATRIX_BENCH);
		placeNode(level, box, ModBlocks.DAMAGED_ARCHIVE_SHELF.defaultBlockState(), 3, 1, mz(10, depth), InvestigationRole.ARCHIVE_DESK);
		placeNode(level, box, ModBlocks.COLLAPSED_TYPE_CABINET.defaultBlockState(), 11, 1, mz(3, depth), InvestigationRole.INK_STATION);
		placeNode(level, box, ModBlocks.FADED_WORKSHOP_PLAQUE.defaultBlockState()
			.setValue(FadedWorkshopPlaqueBlock.FACING, Direction.WEST), 13, 2,
			layoutIndex == 0 ? entranceZ + 1 : entranceZ - 1, InvestigationRole.PLAQUE_CLUE);
		placeNode(level, box, ModBlocks.HIDDEN_FLOOR_COMPARTMENT.defaultBlockState(), 3, 0, mz(12, depth), InvestigationRole.FLOOR_CACHE);

		buildCellar(level, box, 4, mz(12, depth), 2, 6, mz(10, depth), mz(13, depth), Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 3, mz(11, depth));
		placeNode(level, box, ModBlocks.DAMAGED_ARCHIVE_SHELF.defaultBlockState(), 3, -2, mz(11, depth), InvestigationRole.CELLAR_CACHE);
		placeSuspiciousFloors(level, box, width, new int[][] {{2, mz(5, depth)}, {7, mz(12, depth)}, {11, mz(7, depth)}, {5, mz(2, depth)}});

		for (int z : new int[] {2, 4, 6, 8, 10}) {
			placeBlock(level, Blocks.BOOKSHELF.defaultBlockState(), 1, 1, mz(z, depth), box);
			placeBlock(level, Blocks.BOOKSHELF.defaultBlockState(), 1, 2, mz(z, depth), box);
		}
		placeBlock(level, Blocks.LECTERN.defaultBlockState(), 6, 1, mz(5, depth), box);
		placeBlock(level, Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), 10, 1, mz(8, depth), box);
		placeBlock(level, Blocks.BARREL.defaultBlockState(), 12, 1, mz(10, depth), box);
		placeBlock(level, Blocks.CARPET.white().defaultBlockState(), 8, 1, mz(7, depth), box);
		placeLaidPrint(level, box, 10, 2, mz(8, depth), Direction.EAST, ModItems.DECORATIVE_WOODCUT);
		placeLaidPrint(level, box, 12, 2, mz(10, depth), Direction.NORTH, ModItems.DAMAGED_ARCHIVE_PAGE);
		placeHangingPrint(level, box, 13, 2, mz(2, depth), Direction.WEST);
		placeHangingPrint(level, box, 1, 3, mz(13, depth), Direction.EAST);
		placeCobwebs(level, box, new int[][] {{2, 4, mz(2, depth)}, {6, 3, mz(11, depth)}, {12, 4, mz(12, depth)}, {9, 4, mz(4, depth)}});
		createChest(level, box, random, 4, 1, mz(12, depth), SCHOLARLY_LOOT);
	}

	private void buildBurned(WorldGenLevel level, BoundingBox box, RandomSource random) {
		int width = 14;
		int depth = 12;
		prepareSite(level, box, width, depth, Blocks.BLACKSTONE.defaultBlockState());
		fillDecorativeFloor(level, box, width, depth, Blocks.COAL_BLOCK.defaultBlockState(), 5);
		buildPerimeter(level, box, width, depth, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 11);
		int rearDoorX = layoutIndex == 0 ? 3 : 10;
		carveDoor(level, box, rearDoorX, depth - 1);
		// Front entrance is visibly blocked by the fire collapse.
		placeBlock(level, Blocks.BLACKSTONE.defaultBlockState(), mx(6, width), 1, 0, box);
		placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), mx(6, width), 2, 0, box);
		buildRoof(level, box, width, depth, Blocks.DARK_OAK_SLAB.defaultBlockState(), 21, 6, 5, 4);

		placeNode(level, box, ModBlocks.PRESS_FRAME.defaultBlockState(), mx(5, width), 1, 6, InvestigationRole.PRESS_FRAME);
		placeNode(level, box, ModBlocks.PRINTING_DEBRIS.defaultBlockState(), mx(7, width), 1, 5, InvestigationRole.MACHINE_REMAINS);
		placeNode(level, box, ModBlocks.DUSTY_PRINTING_TABLE.defaultBlockState(), mx(3, width), 1, 4, InvestigationRole.MATRIX_BENCH);
		placeNode(level, box, ModBlocks.DAMAGED_ARCHIVE_SHELF.defaultBlockState(), mx(10, width), 1, 8, InvestigationRole.ARCHIVE_DESK);
		placeNode(level, box, ModBlocks.COLLAPSED_TYPE_CABINET.defaultBlockState(), mx(9, width), 1, 3, InvestigationRole.INK_STATION);
		placeNode(level, box, ModBlocks.FADED_WORKSHOP_PLAQUE.defaultBlockState()
			.setValue(FadedWorkshopPlaqueBlock.FACING, Direction.SOUTH),
			layoutIndex == 0 ? rearDoorX + 1 : rearDoorX - 1, 2, depth - 2, InvestigationRole.PLAQUE_CLUE);
		placeNode(level, box, ModBlocks.HIDDEN_FLOOR_COMPARTMENT.defaultBlockState(), mx(9, width), 0, 8, InvestigationRole.FLOOR_CACHE);

		// A concealed crawlspace replaces the formal cellar.
		buildCellar(level, box, mx(11, width), 8, mx(9, width), mx(12, width), 6, 10, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), mx(10, width), 9);
		placeNode(level, box, ModBlocks.PRINTING_DEBRIS.defaultBlockState(), mx(10, width), -2, 9, InvestigationRole.CELLAR_CACHE);
		placeSuspiciousFloors(level, box, width, new int[][] {{2, 2}, {6, 9}, {9, 4}, {4, 7}});

		placeBlock(level, Blocks.IRON_CHAIN.defaultBlockState(), mx(2, width), 3, 3, box);
		placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), mx(11, width), 1, 7, box);
		placeBlock(level, Blocks.CARPET.black().defaultBlockState(), mx(6, width), 1, 6, box);
		placeBlock(level, Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), mx(2, width), 1, 8, box);
		placeBlock(level, Blocks.BARREL.defaultBlockState(), mx(11, width), 1, 4, box);
		placeLaidPrint(level, box, mx(2, width), 2, 8, Direction.WEST, ModItems.DAMAGED_ARCHIVE_PAGE);
		placeLaidPrint(level, box, mx(11, width), 2, 4, Direction.NORTH, ModItems.DECORATIVE_WOODCUT);
		placeHangingPrint(level, box, mx(4, width), 2, 1, Direction.SOUTH);
		placeHangingPrint(level, box, mx(7, width), 3, depth - 2, Direction.NORTH);
		placeCobwebs(level, box, new int[][] {{1, 4, 2}, {8, 3, 9}, {11, 4, 10}, {5, 3, 4}});
		createChest(level, box, random, mx(11, width), -2, 8, BURNED_LOOT);
	}

	private void prepareSite(WorldGenLevel level, BoundingBox box, int width, int depth, BlockState foundation) {
		generateAirBox(level, box, 0, 0, 0, width - 1, HEIGHT - 1, depth - 1);
		for (int x = 0; x < width; x++) {
			for (int z = 0; z < depth; z++) {
				fillColumnDown(level, foundation, x, -1, z, box);
			}
		}
	}

	private void fillDecorativeFloor(
		WorldGenLevel level,
		BoundingBox box,
		int width,
		int depth,
		BlockState damagedFloor,
		int salt
	) {
		for (int x = 1; x < width - 1; x++) {
			for (int z = 1; z < depth - 1; z++) {
				BlockState floor = coordinateHash(x, 0, z, salt, 13) == 0
					? damagedFloor
					: ModBlocks.INK_STAINED_FLOORBOARDS.defaultBlockState();
				placeBlock(level, floor, x, 0, z, box);
			}
		}
	}

	private void buildPerimeter(
		WorldGenLevel level,
		BoundingBox box,
		int width,
		int depth,
		BlockState wall,
		BlockState post,
		int salt
	) {
		for (int y = 0; y <= 5; y++) {
			for (int x = 0; x < width; x++) {
				placeRuinWall(level, box, x, y, 0, wall, post, salt, width, depth);
				placeRuinWall(level, box, x, y, depth - 1, wall, post, salt, width, depth);
			}
			for (int z = 1; z < depth - 1; z++) {
				placeRuinWall(level, box, 0, y, z, wall, post, salt, width, depth);
				placeRuinWall(level, box, width - 1, y, z, wall, post, salt, width, depth);
			}
		}
	}

	private void placeRuinWall(
		WorldGenLevel level,
		BoundingBox box,
		int x,
		int y,
		int z,
		BlockState wall,
		BlockState post,
		int salt,
		int width,
		int depth
	) {
		boolean corner = (x == 0 || x == width - 1) && (z == 0 || z == depth - 1);
		if (y >= 3 && coordinateHash(x, y, z, salt, 17) == 0) {
			return;
		}
		placeBlock(level, corner || y == 0 ? post : wall, x, y, z, box);
	}

	private void carveDoor(WorldGenLevel level, BoundingBox box, int x, int z) {
		placeBlock(level, Blocks.AIR.defaultBlockState(), x, 1, z, box);
		placeBlock(level, Blocks.AIR.defaultBlockState(), x, 2, z, box);
	}

	private void buildRoof(
		WorldGenLevel level,
		BoundingBox box,
		int width,
		int depth,
		BlockState roof,
		int salt,
		int holeX,
		int holeZ,
		int holeRadius
	) {
		for (int x = 0; x < width; x++) {
			for (int z = 0; z < depth; z++) {
				boolean mainCollapse = Math.abs(x - holeX) + Math.abs(z - holeZ) <= holeRadius;
				boolean scattered = coordinateHash(x, 5, z, salt, 11) == 0;
				if (!mainCollapse && !scattered) {
					placeBlock(level, roof, x, 5, z, box);
				}
			}
		}
	}

	private void buildCellar(
		WorldGenLevel level,
		BoundingBox box,
		int hatchX,
		int hatchZ,
		int minX,
		int maxX,
		int minZ,
		int maxZ,
		BlockState wall,
		int nodeX,
		int nodeZ
	) {
		int loX = Math.min(minX, maxX);
		int hiX = Math.max(minX, maxX);
		int loZ = Math.min(minZ, maxZ);
		int hiZ = Math.max(minZ, maxZ);
		for (int x = loX; x <= hiX; x++) {
			for (int z = loZ; z <= hiZ; z++) {
				for (int y = -3; y <= -1; y++) {
					boolean shell = x == loX || x == hiX || z == loZ || z == hiZ || y == -3;
					placeBlock(level, shell ? wall : Blocks.AIR.defaultBlockState(), x, y, z, box);
				}
			}
		}
		BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
		placeBlock(level, Blocks.AIR.defaultBlockState(), hatchX, 0, hatchZ, box);
		for (int y = -2; y <= 0; y++) {
			placeBlock(level, ladder, hatchX, y, hatchZ, box);
		}
		placeBlock(level, Blocks.TORCH.defaultBlockState(), nodeX, -2, nodeZ, box);
	}

	private void placeSuspiciousFloors(WorldGenLevel level, BoundingBox box, int width, int[][] positions) {
		int target = WorkshopLayoutPlan.suspiciousFloorCount(
			variant,
			layoutIndex,
			ModConfig.INSTANCE.suspiciousFloorboardsPerWorkshop
		);
		for (int i = 0; i < target - 1 && i < positions.length; i++) {
			int x = layoutIndex == 0 ? positions[i][0] : width - 1 - positions[i][0];
			placeNode(
				level,
				box,
				ModBlocks.LOOSE_INK_STAINED_FLOORBOARDS.defaultBlockState(),
				x,
				0,
				positions[i][1],
				InvestigationRole.SUSPICIOUS_FLOOR
			);
		}
	}

	private void placeNode(
		WorldGenLevel level,
		BoundingBox box,
		BlockState state,
		int x,
		int y,
		int z,
		InvestigationRole role
	) {
		placeBlock(level, state, x, y, z, box);
		BlockPos worldPos = getWorldPos(x, y, z);
		if (box.isInside(worldPos) && level.getBlockEntity(worldPos) instanceof InvestigationBlockEntity investigation) {
			investigation.configureWorkshop(workshopId, variant, role);
		}
	}

	private void placeLaidPrint(
		WorldGenLevel level,
		BoundingBox box,
		int x,
		int y,
		int z,
		Direction facing,
		Item item
	) {
		placeBlock(
			level,
			ModBlocks.LAID_PAPER.defaultBlockState().setValue(LaidPaperBlock.FACING, facing),
			x, y, z, box
		);
		BlockPos worldPos = getWorldPos(x, y, z);
		if (box.isInside(worldPos) && level.getBlockEntity(worldPos) instanceof LaidPaperBlockEntity paper) {
			paper.setPage(new ItemStack(item));
		}
	}

	private void placeHangingPrint(
		WorldGenLevel level,
		BoundingBox box,
		int x,
		int y,
		int z,
		Direction facing
	) {
		placeBlock(
			level,
			ModBlocks.HANGING_POSTER.defaultBlockState().setValue(HangingPosterBlock.FACING, facing),
			x, y, z, box
		);
	}

	private void placeCobwebs(WorldGenLevel level, BoundingBox box, int[][] positions) {
		for (int[] pos : positions) {
			placeBlock(level, Blocks.COBWEB.defaultBlockState(), pos[0], pos[1], pos[2], box);
		}
	}

	private int mx(int x, int width) {
		return layoutIndex == 0 ? x : width - 1 - x;
	}

	private int mz(int z, int depth) {
		return layoutIndex == 0 ? z : depth - 1 - z;
	}

	private static int coordinateHash(int x, int y, int z, int salt, int modulus) {
		int hash = x * 73428767 ^ y * 912367 ^ z * 4382891 ^ salt * 199999;
		return Math.floorMod(hash, modulus);
	}

	private static ResourceKey<LootTable> loot(String path) {
		return ResourceKey.create(Registries.LOOT_TABLE, EchoesInInk.id(path));
	}
}
