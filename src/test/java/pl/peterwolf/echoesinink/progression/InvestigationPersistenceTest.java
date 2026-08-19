package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import pl.peterwolf.echoesinink.block.InvestigationData;
import pl.peterwolf.echoesinink.block.InvestigationState;

class InvestigationPersistenceTest {
	@Test
	void fullyInvestigatedItemPayloadRetainsOnceOnlyAllocationIdentity() {
		InvestigationData brokenBlockPayload = InvestigationData.of(
			true,
			"starter:floor_cache",
			InvestigationState.FULLY_INVESTIGATED,
			"printshop_7ac21",
			WorkshopVariant.BURNED_CLANDESTINE.id(),
			InvestigationRole.FLOOR_CACHE.id()
		);

		// This is the payload copied to the dropped BlockItem and applied to the
		// replacement block entity. A restored block cannot clean or allocate again.
		assertTrue(brokenBlockPayload.lootGenerated());
		assertEquals(InvestigationState.FULLY_INVESTIGATED, brokenBlockPayload.investigationState());
		assertEquals("printshop_7ac21", brokenBlockPayload.workshopId());
		assertEquals(WorkshopVariant.BURNED_CLANDESTINE.id(), brokenBlockPayload.workshopVariant());
		assertEquals(InvestigationRole.FLOOR_CACHE.id(), brokenBlockPayload.investigationRole());
	}
}
