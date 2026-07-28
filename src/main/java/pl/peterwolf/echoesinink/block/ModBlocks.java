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

/**
 * Historical workshop blocks. Phase 1 ships test debris + matrix + placeholders for the lens.
 * Phase 2 expands investigation behaviour.
 *
 * Note: avoid ofFullCopy on axis-dependent log blocks — their light/occlusion lambdas
 * reference the AXIS property and crash on plain Blocks.
 */
public final class ModBlocks {
	private static final List<Block> ALL = new ArrayList<>();
	private static final List<Item> BLOCK_ITEMS = new ArrayList<>();

	public static final PrintingDebrisBlock PRINTING_DEBRIS = register(
		"printing_debris",
		key -> new PrintingDebrisBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
			.setId(key)
			.strength(1.5F)
			.sound(SoundType.WOOD)),
		BlockItem::new
	);

	public static final CarvedWoodenMatrixBlock CARVED_WOODEN_MATRIX = register(
		"carved_wooden_matrix",
		key -> new CarvedWoodenMatrixBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
			.setId(key)
			.strength(2.0F)
			.sound(SoundType.WOOD)),
		BlockItem::new
	);

	public static final Block DUSTY_PRINTING_TABLE = register(
		"dusty_printing_table",
		key -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).setId(key)),
		BlockItem::new
	);

	public static final Block DAMAGED_ARCHIVE_SHELF = register(
		"damaged_archive_shelf",
		key -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BOOKSHELF).setId(key)),
		BlockItem::new
	);

	public static final Block BROKEN_PRESS_FRAME = register(
		"broken_press_frame",
		key -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
			.setId(key)
			.strength(2.5F)
			.sound(SoundType.WOOD)
			.noOcclusion()),
		BlockItem::new
	);

	public static final Block COLLAPSED_TYPE_CABINET = register(
		"collapsed_type_cabinet",
		key -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).setId(key)),
		BlockItem::new
	);

	public static final Block INK_STAINED_FLOORBOARDS = register(
		"ink_stained_floorboards",
		key -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(key)),
		BlockItem::new
	);

	public static final Block FADED_WORKSHOP_PLAQUE = register(
		"faded_workshop_plaque",
		key -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
			.setId(key)
			.strength(1.0F)
			.sound(SoundType.WOOD)),
		BlockItem::new
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
}
