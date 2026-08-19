package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class PrintshopSupplyLootTest {
	private static final List<String> CHEST_TABLES = List.of(
		"printshop_supplies",
		"printshop_storage",
		"printshop_hidden",
		"printshop_rural",
		"printshop_town",
		"printshop_scholarly",
		"printshop_burned"
	);

	private static final List<String> REQUIRED = List.of(
		"minecraft:paper",
		"echoes_in_ink:blank_archive_page",
		"echoes_in_ink:ink_ball",
		"echoes_in_ink:press_screw",
		"echoes_in_ink:press_handle",
		"echoes_in_ink:press_platen",
		"echoes_in_ink:press_carriage"
	);

	@Test
	void everyPrintshopChestTableSuppliesPaperInkAndPressParts() throws IOException {
		String supplies = readChest("printshop_supplies");
		for (String item : REQUIRED) {
			assertTrue(supplies.contains(item), "printshop_supplies missing " + item);
		}

		for (String table : CHEST_TABLES) {
			if ("printshop_supplies".equals(table)) {
				continue;
			}
			String json = readChest(table);
			assertTrue(
				json.contains("echoes_in_ink:chests/printshop_supplies"),
				table + " must include the shared almost-full supply kit"
			);
		}
	}

	@Test
	void debrisDismantleAlwaysYieldsPrintingSuppliesAndPressHardware() throws IOException {
		String source = Files.readString(Path.of(
			"src/main/java/pl/peterwolf/echoesinink/block/InvestigationLoot.java"
		));
		assertTrue(source.contains("dismantlePartial"));
		assertTrue(source.contains("dismantleComplete"));
		assertTrue(source.contains("Items.PAPER"));
		assertTrue(source.contains("ModItems.BLANK_ARCHIVE_PAGE"));
		assertTrue(source.contains("ModItems.INK_BALL"));
		assertTrue(source.contains("ModItems.PRESS_SCREW"));
		assertTrue(source.contains("ModItems.PRESS_HANDLE"));
		assertTrue(source.contains("ModItems.PRESS_PLATEN"));
		assertTrue(source.contains("ModItems.PRESS_CARRIAGE"));
	}

	private static String readChest(String name) throws IOException {
		return Files.readString(Path.of(
			"src/main/resources/data/echoes_in_ink/loot_table/chests/" + name + ".json"
		));
	}
}
