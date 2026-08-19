package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PressBreakDropsContractTest {
	@Test
	void breakingThePressReturnsTheFrameInsteadOfTheAssembledBlock() throws IOException {
		String entity = Files.readString(Path.of(
			"src/main/java/pl/peterwolf/echoesinink/block/entity/PrintingPressBlockEntity.java"
		));
		String loot = Files.readString(Path.of(
			"src/main/resources/data/echoes_in_ink/loot_table/blocks/printing_press.json"
		));
		assertTrue(entity.contains("ModBlocks.PRESS_FRAME"));
		assertTrue(entity.contains("giveOrDropBroken"));
		assertFalse(loot.contains("echoes_in_ink:printing_press"));
	}
}
