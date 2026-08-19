package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class LegacyWorkshopBinderTest {
	@Test
	void preRebalanceRuralLayoutReceivesEveryMandatoryRoleExactlyOnce() {
		List<LegacyWorkshopBinder.NodeKind> legacyNodes = List.of(
			LegacyWorkshopBinder.NodeKind.PRESS_FRAME,
			LegacyWorkshopBinder.NodeKind.PRESS_FRAME,
			LegacyWorkshopBinder.NodeKind.TABLE,
			LegacyWorkshopBinder.NodeKind.CABINET,
			LegacyWorkshopBinder.NodeKind.CABINET,
			LegacyWorkshopBinder.NodeKind.SHELF,
			LegacyWorkshopBinder.NodeKind.SHELF,
			LegacyWorkshopBinder.NodeKind.SHELF,
			LegacyWorkshopBinder.NodeKind.DEBRIS,
			LegacyWorkshopBinder.NodeKind.DEBRIS,
			LegacyWorkshopBinder.NodeKind.DEBRIS,
			LegacyWorkshopBinder.NodeKind.DEBRIS,
			LegacyWorkshopBinder.NodeKind.DEBRIS,
			LegacyWorkshopBinder.NodeKind.PLAQUE
		);

		List<InvestigationRole> roles = LegacyWorkshopBinder.planRoles(legacyNodes);
		for (InvestigationRole required : List.of(
			InvestigationRole.PRESS_FRAME,
			InvestigationRole.MACHINE_REMAINS,
			InvestigationRole.CELLAR_CACHE,
			InvestigationRole.FLOOR_CACHE,
			InvestigationRole.MATRIX_BENCH,
			InvestigationRole.ARCHIVE_DESK,
			InvestigationRole.INK_STATION,
			InvestigationRole.PLAQUE_CLUE
		)) {
			assertEquals(1, roles.stream().filter(role -> role == required).count(), required.id());
		}
	}
}
