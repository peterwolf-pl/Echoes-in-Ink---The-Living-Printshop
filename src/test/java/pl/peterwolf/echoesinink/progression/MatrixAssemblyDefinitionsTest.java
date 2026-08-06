package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MatrixAssemblyDefinitionsTest {
	@Test
	void everyAssemblyHasUniquePartsAndAPrintingUnlock() {
		Set<String> results = new HashSet<>();
		for (MatrixAssemblyDefinition definition : MatrixAssemblyDefinitions.all()) {
			assertEquals(definition.ingredients().size(), new HashSet<>(definition.ingredients()).size(), definition.id());
			assertTrue(results.add(definition.result()), definition.result());
			assertFalse(definition.printingRecipes().isEmpty(), definition.id());
		}
	}

	@Test
	void dataPackRecipesMatchTheAssemblyContracts() throws IOException {
		Path recipeRoot = Path.of("src/main/resources/data/echoes_in_ink/recipe");
		for (MatrixAssemblyDefinition definition : MatrixAssemblyDefinitions.all()) {
			String json = Files.readString(recipeRoot.resolve(definition.result() + ".json"));
			for (String ingredient : definition.ingredients()) {
				assertTrue(json.contains("echoes_in_ink:" + ingredient), definition.id() + " missing " + ingredient);
			}
			assertTrue(json.contains("echoes_in_ink:" + definition.result()), definition.id());
		}
	}
}
