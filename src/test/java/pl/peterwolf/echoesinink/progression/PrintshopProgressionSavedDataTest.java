package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PrintshopProgressionSavedDataTest {
	@Test
	void abandoningOneWorkshopCannotDisableStarterPartsBeforeARealPressRun() {
		PrintshopProgressionSavedData data = new PrintshopProgressionSavedData();

		assertTrue(data.starterRewardsAllowed("printshop_first"));
		assertTrue(data.starterRewardsAllowed("printshop_second"));
		assertTrue(data.starterRewardsAllowed("printshop_third"));

		data.markBasicPressOperated();
		assertTrue(data.basicPressOperated());
		assertFalse(data.starterRewardsAllowed("printshop_fourth"));
	}
}
