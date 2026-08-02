package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GenericLootAuditTest {
	@Test
	void restoredChronicleIsAbsentFromGenericLootTablesAndInvestigationRolls() throws IOException {
		Path lootRoot = Path.of("src/main/resources/data/echoes_in_ink/loot_table");
		try (var files = Files.walk(lootRoot)) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				assertFalse(
					Files.readString(file).contains("echoes_in_ink:restored_chronicle_page"),
					() -> "Progression skip in generic loot: " + file
				);
			}
		}

		Path investigationLoot = Path.of(
			"src/main/java/pl/peterwolf/echoesinink/block/InvestigationLoot.java"
		);
		assertFalse(
			Files.readString(investigationLoot).contains("ModItems.RESTORED_CHRONICLE_PAGE"),
			"Restored Chronicle Page must only be produced by the press recipe"
		);
	}
}
