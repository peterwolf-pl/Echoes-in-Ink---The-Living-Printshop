package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ItemDescriptionCoverageTest {
	private static final Pattern REGISTERED_ID = Pattern.compile("\\bregister\\(\\s*\"([^\"]+)\"");

	@Test
	void everyRegisteredModItemAndBlockItemExplainsItsDescriptionAndPurpose() throws IOException {
		Set<String> itemIds = ids(Path.of("src/main/java/pl/peterwolf/echoesinink/item/ModItems.java"));
		Set<String> blockItemIds = ids(Path.of("src/main/java/pl/peterwolf/echoesinink/block/ModBlocks.java"));
		assertEquals(31, itemIds.size(), "Update expected item count when registering a new item");
		assertEquals(11, blockItemIds.size(), "Update expected block-item count when registering a new block item");

		for (String language : List.of("en_us", "pl_pl")) {
			String json = Files.readString(Path.of(
				"src/main/resources/assets/echoes_in_ink/lang/" + language + ".json"
			));
			for (String id : itemIds) {
				assertKey(json, "item.echoes_in_ink." + id, language);
			}
			for (String id : blockItemIds) {
				assertKey(json, "block.echoes_in_ink." + id, language);
			}
		}
	}

	private static Set<String> ids(Path source) throws IOException {
		Set<String> result = new LinkedHashSet<>();
		var matcher = REGISTERED_ID.matcher(Files.readString(source));
		while (matcher.find()) {
			result.add(matcher.group(1));
		}
		return result;
	}

	private static void assertKey(String json, String base, String language) {
		assertTrue(json.contains("\"" + base + "\""), language + " missing name " + base);
		assertTrue(json.contains("\"" + base + ".desc\""), language + " missing description " + base);
		assertTrue(json.contains("\"" + base + ".purpose\""), language + " missing purpose " + base);
	}
}
