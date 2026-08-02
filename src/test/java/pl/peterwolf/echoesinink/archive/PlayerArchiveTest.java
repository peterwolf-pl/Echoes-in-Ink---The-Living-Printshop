package pl.peterwolf.echoesinink.archive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PlayerArchiveTest {
	@Test
	void tracksProgressCategoriesAndCopiesWithoutSharingMutation() {
		PlayerArchive archive = new PlayerArchive();
		assertTrue(archive.unlock(ArchiveEntries.PART_SCREW.id()));
		assertTrue(archive.recordWorkshop("printshop_a1", "rural_woodcut"));
		assertTrue(archive.recordRecoveredMaterial("upper_matrix_fragment"));
		assertTrue(archive.recordAvailableRecipe("village_chronicle_print"));
		assertTrue(archive.recordPrintedWork("village_chronicle_print"));
		assertTrue(archive.recordUnresolvedClue("clue_hidden"));

		PlayerArchive copy = archive.copy();
		archive.clear();

		assertTrue(copy.has(ArchiveEntries.PART_SCREW));
		assertEquals(Set.of("printshop_a1"), copy.workshopIds());
		assertEquals(Set.of("rural_woodcut"), copy.workshopVariants());
		assertEquals(Set.of("upper_matrix_fragment"), copy.recoveredMaterials());
		assertEquals(Set.of("village_chronicle_print"), copy.availableRecipes());
		assertEquals(Set.of("village_chronicle_print"), copy.printedWorks());
		assertEquals(Set.of("clue_hidden"), copy.unresolvedClues());
		assertFalse(archive.has(ArchiveEntries.PART_SCREW));
	}
}
