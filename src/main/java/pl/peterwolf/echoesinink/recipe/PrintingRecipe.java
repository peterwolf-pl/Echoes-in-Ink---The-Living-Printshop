package pl.peterwolf.echoesinink.recipe;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side printing recipe. Matched only on the server when the press starts.
 */
public record PrintingRecipe(
	Identifier id,
	Item matrix,
	Item paper,
	Item ink,
	ItemStack output,
	int durationTicks,
	int requiredTier
) {
	public boolean matches(ItemStack matrixStack, ItemStack paperStack, ItemStack inkStack) {
		return !matrixStack.isEmpty()
			&& !paperStack.isEmpty()
			&& !inkStack.isEmpty()
			&& matrixStack.is(matrix)
			&& paperStack.is(paper)
			&& inkStack.is(ink);
	}

	public ItemStack createOutput() {
		return output.copy();
	}
}
