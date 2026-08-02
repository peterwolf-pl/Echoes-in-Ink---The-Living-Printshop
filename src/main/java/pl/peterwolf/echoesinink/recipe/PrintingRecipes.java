package pl.peterwolf.echoesinink.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
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
			list.add(new PrintingRecipe(
				EchoesInInk.id("printers_instruction_sheet"),
				ModItems.WOODEN_PRINTING_MATRIX,
				ModItems.BLANK_ARCHIVE_PAGE,
				ModItems.INK_BALL,
				new ItemStack(ModItems.PRINTERS_INSTRUCTION_SHEET),
				60,
				1
			));
			list.add(new PrintingRecipe(
				EchoesInInk.id("restored_chronicle_page"),
				ModItems.WOODEN_PRINTING_MATRIX,
				ModItems.DAMAGED_ARCHIVE_PAGE,
				ModItems.INK_BALL,
				new ItemStack(ModItems.RESTORED_CHRONICLE_PAGE),
				80,
				1
			));
			list.add(new PrintingRecipe(
				EchoesInInk.id("decorative_woodcut"),
				ModItems.WOODEN_PRINTING_MATRIX,
				ModItems.BLANK_ARCHIVE_PAGE,
				ModItems.INK_PAD,
				new ItemStack(ModItems.DECORATIVE_WOODCUT),
				50,
				1
			));
			list.add(new PrintingRecipe(
				EchoesInInk.id("printed_warning_poster"),
				ModItems.METAL_TYPE_PIECE,
				ModItems.BLANK_ARCHIVE_PAGE,
				ModItems.INK_BALL,
				new ItemStack(ModItems.PRINTED_WARNING_POSTER),
				40,
				1
			));
			// Composed metal type is charged with either historical ink tool.
			// The press accepts both, so both must produce the same impression.
			list.add(new PrintingRecipe(
				EchoesInInk.id("printed_warning_poster_ink_pad"),
				ModItems.METAL_TYPE_PIECE,
				ModItems.BLANK_ARCHIVE_PAGE,
				ModItems.INK_PAD,
				new ItemStack(ModItems.PRINTED_WARNING_POSTER),
				40,
				1
			));
			list.add(new PrintingRecipe(
				EchoesInInk.id("workshop_map_fragment"),
				ModItems.CHARCOAL_RUBBING,
				ModItems.BLANK_ARCHIVE_PAGE,
				ModItems.INK_PAD,
				new ItemStack(ModItems.WORKSHOP_MAP_FRAGMENT),
				70,
				1
			));
			list.add(new PrintingRecipe(
				EchoesInInk.id("village_chronicle_print"),
				ModItems.VILLAGE_CHRONICLE_MATRIX,
				ModItems.BLANK_ARCHIVE_PAGE,
				ModItems.INK_BALL,
				new ItemStack(ModItems.VILLAGE_CHRONICLE_PRINT),
				70,
				1
			));
			list.add(new PrintingRecipe(
				EchoesInInk.id("forbidden_notice_print"),
				ModItems.FORBIDDEN_NOTICE_FORME,
				ModItems.BLANK_ARCHIVE_PAGE,
				ModItems.INK_PAD,
				new ItemStack(ModItems.FORBIDDEN_NOTICE_PRINT),
				70,
				1
			));
			recipes = List.copyOf(list);
		}
		return recipes;
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
			.map(recipe -> recipe.id().getPath())
			.distinct()
			.toList();
	}
}
