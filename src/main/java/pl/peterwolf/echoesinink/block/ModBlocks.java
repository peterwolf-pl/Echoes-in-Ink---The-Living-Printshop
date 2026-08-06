package pl.peterwolf.echoesinink.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.item.InvestigatableBlockItem;

/**
 * Historical workshop blocks with investigation support.
 * Avoid ofFullCopy on axis-dependent log blocks.
 */
public final class ModBlocks {
	private static final List<Block> ALL = new ArrayList<>();
	private static final List<Item> BLOCK_ITEMS = new ArrayList<>();

	public static final PrintingDebrisBlock PRINTING_DEBRIS = register(
		"printing_debris",
		key -> new PrintingDebrisBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
			.setId(key)
			.strength(1.5F)
			.sound(SoundType.WOOD)
			.noOcclusion()),
		InvestigatableBlockItem::new
	);

	public static final CarvedWoodenMatrixBlock CARVED_WOODEN_MATRIX = register(
		"carved_wooden_matrix",
		key -> new CarvedWoodenMatrixBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
			.setId(key)
			.strength(2.0F)
			.sound(SoundType.WOOD)),
		BlockItem::new
	);

	public static final DustyPrintingTableBlock DUSTY_PRINTING_TABLE = register(
		"dusty_printing_table",
		key -> new DustyPrintingTableBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
				.setId(key)
				.noOcclusion()),
		InvestigatableBlockItem::new
	);

	public static final InvestigatableBlock DAMAGED_ARCHIVE_SHELF = register(
		"damaged_archive_shelf",
		key -> new InvestigatableBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.BOOKSHELF).setId(key),
			InvestigationLoot.Profile.SHELF),
		InvestigatableBlockItem::new
	);

	public static final InvestigatableBlock PRESS_FRAME = register(
		"press_frame",
		key -> new InvestigatableBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
				.setId(key)
				.strength(2.5F)
				.sound(SoundType.WOOD)
				.noOcclusion(),
			InvestigationLoot.Profile.PRESS),
		InvestigatableBlockItem::new
	);

	public static final TypeCabinetBlock COLLAPSED_TYPE_CABINET = register(
		"collapsed_type_cabinet",
		key -> new TypeCabinetBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL)
				.setId(key)
				.noOcclusion()),
		InvestigatableBlockItem::new
	);

	public static final DecorativeFloorboardsBlock INK_STAINED_FLOORBOARDS = register(
		"ink_stained_floorboards",
		key -> new DecorativeFloorboardsBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(key)),
		BlockItem::new
	);

	public static final InvestigatableBlock LOOSE_INK_STAINED_FLOORBOARDS = register(
		"loose_ink_stained_floorboards",
		key -> new InvestigatableBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(key),
			InvestigationLoot.Profile.FLOOR),
		InvestigatableBlockItem::new
	);

	public static final InvestigatableBlock HIDDEN_FLOOR_COMPARTMENT = register(
		"hidden_floor_compartment",
		key -> new InvestigatableBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
				.setId(key)
				.strength(2.0F)
				.sound(SoundType.WOOD),
			InvestigationLoot.Profile.FLOOR_HIDDEN),
		InvestigatableBlockItem::new
	);

	public static final FadedWorkshopPlaqueBlock FADED_WORKSHOP_PLAQUE = register(
		"faded_workshop_plaque",
		key -> new FadedWorkshopPlaqueBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
				.setId(key)
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.noOcclusion()),
		InvestigatableBlockItem::new
	);

	/** Phase 4 — historical screw printing press controller. */
	public static final PrintingPressBlock PRINTING_PRESS = register(
		"printing_press",
		key -> new PrintingPressBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
			.setId(key)
			.strength(2.5F, 6.0F)
			.sound(SoundType.WOOD)
			.noOcclusion()),
		BlockItem::new
	);

	/**
	 * Hung warning poster (placed by {@link pl.peterwolf.echoesinink.item.PrintedWarningPosterItem}).
	 * No separate BlockItem — drops the print item.
	 */
	public static final HangingPosterBlock HANGING_POSTER = registerBlockOnly(
		"hanging_poster",
		key -> new HangingPosterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
			.setId(key)
			.strength(0.2F)
			.sound(SoundType.WOOL)
			.noOcclusion())
	);

	/** Single archive page laid on a table / floor (placed by blank/damaged page items). */
	public static final LaidPaperBlock LAID_PAPER = registerBlockOnly(
		"laid_paper",
		key -> new LaidPaperBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_CARPET)
			.setId(key)
			.strength(0.1F)
			.sound(SoundType.WOOL)
			.noOcclusion())
	);

	private ModBlocks() {}

	public static void init() {
		// static registration
	}

	public static List<Block> all() {
		return List.copyOf(ALL);
	}

	public static List<Item> blockItems() {
		return List.copyOf(BLOCK_ITEMS);
	}

	private static <T extends Block> T register(
		String path,
		Function<ResourceKey<Block>, T> blockFactory,
		BiFunction<Block, Item.Properties, BlockItem> itemFactory
	) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, EchoesInInk.id(path));
		T block = Registry.register(BuiltInRegistries.BLOCK, blockKey, blockFactory.apply(blockKey));
		ALL.add(block);

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, EchoesInInk.id(path));
		BlockItem item = itemFactory.apply(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
		item.registerBlocks(Item.BY_BLOCK, item);
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);
		BLOCK_ITEMS.add(item);
		return block;
	}

	/** Block without a dedicated BlockItem (item lives in ModItems). */
	private static <T extends Block> T registerBlockOnly(
		String path,
		Function<ResourceKey<Block>, T> blockFactory
	) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, EchoesInInk.id(path));
		T block = Registry.register(BuiltInRegistries.BLOCK, blockKey, blockFactory.apply(blockKey));
		ALL.add(block);
		return block;
	}
}
