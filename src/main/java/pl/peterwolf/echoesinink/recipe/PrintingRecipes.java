package pl.peterwolf.echoesinink.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.item.ModItems;

/**
 * Built-in printing recipes for the ModJam MVP.
 * Lazily built after item registry holders are fully bound.
 */
public final class PrintingRecipes {
	private static List<PrintingRecipe> recipes;

	private PrintingRecipes() {}

	public static void init() {
		// Defer construction until first match — registry components must be bound.
	}

	private static List<PrintingRecipe> recipes() {
		if (recipes == null) {
			List<PrintingRecipe> list = new ArrayList<>();
			// Wooden matrix: accept either historical ink tool (ball or pad).
			addBothInks(list, "printers_instruction_sheet",
				ModItems.WOODEN_PRINTING_MATRIX, ModItems.BLANK_ARCHIVE_PAGE,
				new ItemStack(ModItems.PRINTERS_INSTRUCTION_SHEET), 60);
			addBothInks(list, "restored_chronicle_page",
				ModItems.WOODEN_PRINTING_MATRIX, ModItems.DAMAGED_ARCHIVE_PAGE,
				new ItemStack(ModItems.RESTORED_CHRONICLE_PAGE), 80);
			addBothInks(list, "decorative_woodcut",
				ModItems.WOODEN_PRINTING_MATRIX, ModItems.BLANK_ARCHIVE_PAGE,
				new ItemStack(ModItems.DECORATIVE_WOODCUT), 50);
			// Metal type / specialist forms: also both inks.
			addBothInks(list, "printed_warning_poster",
				ModItems.METAL_TYPE_PIECE, ModItems.BLANK_ARCHIVE_PAGE,
				new ItemStack(ModItems.PRINTED_WARNING_POSTER), 40);
			addBothInks(list, "workshop_map_fragment",
				ModItems.CHARCOAL_RUBBING, ModItems.BLANK_ARCHIVE_PAGE,
				new ItemStack(ModItems.WORKSHOP_MAP_FRAGMENT), 70);
			addBothInks(list, "village_chronicle_print",
				ModItems.VILLAGE_CHRONICLE_MATRIX, ModItems.BLANK_ARCHIVE_PAGE,
				new ItemStack(ModItems.VILLAGE_CHRONICLE_PRINT), 70);
			addBothInks(list, "forbidden_notice_print",
				ModItems.FORBIDDEN_NOTICE_FORME, ModItems.BLANK_ARCHIVE_PAGE,
				new ItemStack(ModItems.FORBIDDEN_NOTICE_PRINT), 70);
			recipes = List.copyOf(list);
		}
		return recipes;
	}

	/** Blank archive paper and vanilla paper are interchangeable press stock. */
	public static boolean isBlankPrintPaper(Item item) {
		return item == ModItems.BLANK_ARCHIVE_PAGE || item == Items.PAPER;
	}

	public static boolean isPressPaper(Item item) {
		return isBlankPrintPaper(item) || item == ModItems.DAMAGED_ARCHIVE_PAGE;
	}

	public static Optional<PrintingRecipe> findMatch(ItemStack matrix, ItemStack paper, ItemStack ink) {
		for (PrintingRecipe recipe : recipes()) {
			if (recipe.matches(matrix, paper, ink)) {
				return Optional.of(recipe);
			}
		}
		return Optional.empty();
	}

	public static List<PrintingRecipe> all() {
		return recipes();
	}

	public static List<String> recipeIdsForMatrix(ItemStack matrix) {
		if (matrix.isEmpty()) {
			return List.of();
		}
		return recipes().stream()
			.filter(recipe -> recipe.matrix() == matrix.getItem())
			.map(recipe -> recipe.id().getPath().replace("_ink_pad", "").replace("_ink_ball", ""))
			.distinct()
			.toList();
	}

	private static void addBothInks(
		List<PrintingRecipe> list,
		String baseId,
		net.minecraft.world.item.Item matrix,
		net.minecraft.world.item.Item paper,
		ItemStack output,
		int durationTicks
	) {
		list.add(new PrintingRecipe(
			EchoesInInk.id(baseId),
			matrix,
			paper,
			ModItems.INK_BALL,
			output.copy(),
			durationTicks,
			1
		));
		list.add(new PrintingRecipe(
			EchoesInInk.id(baseId + "_ink_pad"),
			matrix,
			paper,
			ModItems.INK_PAD,
			output.copy(),
			durationTicks,
			1
		));
	}
}
