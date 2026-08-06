package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WorkshopVariantSelectorTest {
	@Test
	void selectionIsStableAndCanBeDisabledForCompatibility() {
		for (int i = 0; i < 128; i++) {
			String id = "printshop_" + Integer.toHexString(i * 73856093);
			assertEquals(
				WorkshopVariantSelector.select(id, true),
				WorkshopVariantSelector.select(id, true)
			);
			assertEquals(
				new WorkshopVariantSelector.Selection(WorkshopVariant.RURAL_WOODCUT, 0),
				WorkshopVariantSelector.select(id, false)
			);
		}
	}

	@Test
	void stableSelectorReachesAllFourTypesAndBothLayouts() {
		Set<WorkshopVariant> variants = EnumSet.noneOf(WorkshopVariant.class);
		Set<Integer> layouts = new HashSet<>();
		for (int i = 0; i < 2048; i++) {
			var selection = WorkshopVariantSelector.select("printshop_distribution_" + i, true);
			variants.add(selection.variant());
			layouts.add(selection.layoutIndex());
		}
		assertEquals(EnumSet.allOf(WorkshopVariant.class), variants);
		assertEquals(Set.of(0, 1), layouts);
	}

	@Test
	void storedIdentityRoundTripsAndUsesMigrationSafeDefaults() {
		for (WorkshopVariant variant : WorkshopVariant.values()) {
			for (int layout = 0; layout < 2; layout++) {
				WorkshopIdentity original = new WorkshopIdentity("printshop_saved", variant, layout);
				WorkshopIdentity loaded = WorkshopIdentity.fromStored(
					original.workshopId(), original.variant().id(), original.layoutIndex()
				);
				assertEquals(original, loaded);
			}
		}
		WorkshopIdentity migrated = WorkshopIdentity.fromStored("", "unknown_old_variant", 7);
		assertEquals("printshop_unknown", migrated.workshopId());
		assertEquals(WorkshopVariant.RURAL_WOODCUT, migrated.variant());
		assertTrue(migrated.layoutIndex() == 0 || migrated.layoutIndex() == 1);
	}
}
