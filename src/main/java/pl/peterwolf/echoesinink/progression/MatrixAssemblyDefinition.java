package pl.peterwolf.echoesinink.progression;

import java.util.List;

/**
 * Registry-id-only description of an assembled printing form.
 *
 * <p>The actual crafting operations live in data-pack recipe JSON. Keeping the
 * progression contract here lets tests and the archive describe future forms
 * without adding another matrix-specific conditional.</p>
 */
public record MatrixAssemblyDefinition(
	String id,
	List<String> ingredients,
	String result,
	List<String> printingRecipes
) {
	public MatrixAssemblyDefinition {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("id");
		}
		if (ingredients == null || ingredients.isEmpty()) {
			throw new IllegalArgumentException("ingredients");
		}
		if (result == null || result.isBlank()) {
			throw new IllegalArgumentException("result");
		}
		ingredients = List.copyOf(ingredients);
		printingRecipes = printingRecipes == null ? List.of() : List.copyOf(printingRecipes);
	}
}
