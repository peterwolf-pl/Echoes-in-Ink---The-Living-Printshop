package pl.peterwolf.echoesinink.structure;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import pl.peterwolf.echoesinink.progression.WorkshopIdentity;

class VillagePrintshopLayoutTest {
	@Test
	void largeVillageBiomesGetTwoPrintshopsAndSmallOnesGetOne() {
		assertEquals(2, VillagePrintshopLayout.printshopCount("plains"));
		assertEquals(2, VillagePrintshopLayout.printshopCount("meadow"));
		assertEquals(2, VillagePrintshopLayout.printshopCount("savanna"));
		assertEquals(2, VillagePrintshopLayout.printshopCount("taiga"));
		assertEquals(1, VillagePrintshopLayout.printshopCount("desert"));
		assertEquals(1, VillagePrintshopLayout.printshopCount("snowy_plains"));
		assertFalse(VillagePrintshopLayout.isLargeVillageBiome("ocean"));
		assertTrue(VillagePrintshopLayout.isLargeVillageBiome("plains"));
	}

	@Test
	void oppositePrintshopsDoNotOverlap() {
		int[] first = VillagePrintshopLayout.originOffset(0, 42L);
		int[] second = VillagePrintshopLayout.originOffset(1, 42L);
		int firstMaxX = first[0] + VillagePrintshopLayout.PRINTSHOP_WIDTH;
		int firstMaxZ = first[1] + VillagePrintshopLayout.PRINTSHOP_DEPTH;
		int secondMaxX = second[0] + VillagePrintshopLayout.PRINTSHOP_WIDTH;
		int secondMaxZ = second[1] + VillagePrintshopLayout.PRINTSHOP_DEPTH;
		boolean overlapX = first[0] < secondMaxX && second[0] < firstMaxX;
		boolean overlapZ = first[1] < secondMaxZ && second[1] < firstMaxZ;
		assertFalse(overlapX && overlapZ);
	}

	@Test
	void secondWorkshopInTheSameChunkGetsADistinctId() {
		var chunk = new net.minecraft.world.level.ChunkPos(12, -7);
		assertEquals(WorkshopIdentity.idForChunk(chunk), WorkshopIdentity.idForChunk(chunk, 0));
		assertTrue(!WorkshopIdentity.idForChunk(chunk, 0).equals(WorkshopIdentity.idForChunk(chunk, 1)));
	}

	@Test
	void structureSetSharesTheVanillaVillageGrid() throws IOException {
		String json = Files.readString(Path.of(
			"src/main/resources/data/echoes_in_ink/worldgen/structure_set/abandoned_printshops.json"
		));
		assertTrue(json.contains("\"salt\": 10387312"));
		assertTrue(json.contains("\"spacing\": 34"));
		assertTrue(json.contains("\"separation\": 8"));
	}

	@Test
	void printshopBiomesAreTheVanillaVillageBiomes() throws IOException {
		String json = Files.readString(Path.of(
			"src/main/resources/data/echoes_in_ink/tags/worldgen/biome/has_structure/abandoned_printshop.json"
		));
		assertTrue(json.contains("village_plains"));
		assertTrue(json.contains("village_desert"));
		assertTrue(json.contains("village_savanna"));
		assertTrue(json.contains("village_snowy"));
		assertTrue(json.contains("village_taiga"));
	}

	@Test
	void oppositeOffsetIsStableForAGivenSeed() {
		assertArrayEquals(
			VillagePrintshopLayout.originOffset(0, 99L),
			VillagePrintshopLayout.originOffset(0, 99L)
		);
	}
}
