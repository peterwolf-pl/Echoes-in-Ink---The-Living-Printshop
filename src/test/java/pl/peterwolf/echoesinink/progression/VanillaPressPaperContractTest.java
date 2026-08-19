package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class VanillaPressPaperContractTest {
	@Test
	void pressRecipesTreatVanillaPaperAsBlankStock() throws IOException {
		String recipes = Files.readString(Path.of(
			"src/main/java/pl/peterwolf/echoesinink/recipe/PrintingRecipes.java"
		));
		String match = Files.readString(Path.of(
			"src/main/java/pl/peterwolf/echoesinink/recipe/PrintingRecipe.java"
		));
		String press = Files.readString(Path.of(
			"src/main/java/pl/peterwolf/echoesinink/block/entity/PrintingPressBlockEntity.java"
		));
		assertTrue(recipes.contains("Items.PAPER"));
		assertTrue(recipes.contains("isBlankPrintPaper"));
		assertTrue(recipes.contains("isPressPaper"));
		assertTrue(match.contains("isBlankPrintPaper"));
		assertTrue(press.contains("PrintingRecipes.isPressPaper"));
	}
}
