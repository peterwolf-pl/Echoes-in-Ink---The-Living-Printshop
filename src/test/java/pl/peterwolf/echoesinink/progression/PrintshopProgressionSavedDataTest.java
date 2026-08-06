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
		assertTrue(data.claimStarterSupply("printshop_first"));
		assertTrue(data.hasStarterSupply("printshop_first"));
		assertFalse(data.claimStarterSupply("printshop_first"));
		assertTrue(data.claimStarterSupply("printshop_second"));

		data.markBasicPressOperated();
		assertTrue(data.basicPressOperated());
		assertFalse(data.starterRewardsAllowed("printshop_fourth"));
		assertFalse(data.claimStarterSupply("printshop_fourth"));
	}
}
