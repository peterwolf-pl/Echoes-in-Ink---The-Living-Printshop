package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class VariantStructureContractTest {
	private static final List<String> BUILDERS = List.of("Rural", "Town", "Scholarly", "Burned");
	private static final List<InvestigationRole> REQUIRED_ROLES = List.of(
		InvestigationRole.PRESS_FRAME,
		InvestigationRole.MACHINE_REMAINS,
		InvestigationRole.CELLAR_CACHE,
		InvestigationRole.FLOOR_CACHE,
		InvestigationRole.MATRIX_BENCH,
		InvestigationRole.ARCHIVE_DESK,
		InvestigationRole.INK_STATION,
		InvestigationRole.PLAQUE_CLUE
	);

	@Test
	void everyProceduralBuilderPlacesEveryStarterRoleExactlyOnce() throws IOException {
		String source = Files.readString(Path.of(
			"src/main/java/pl/peterwolf/echoesinink/structure/AbandonedPrintshopPiece.java"
		));
		for (int i = 0; i < BUILDERS.size(); i++) {
			String name = BUILDERS.get(i);
			int start = source.indexOf("private void build" + name + "(");
			int end = i + 1 < BUILDERS.size()
				? source.indexOf("private void build" + BUILDERS.get(i + 1) + "(")
				: source.indexOf("private void prepareSite(");
			assertTrue(start >= 0 && end > start, name);
			String builder = source.substring(start, end);
			for (InvestigationRole role : REQUIRED_ROLES) {
				String token = "InvestigationRole." + role.name();
				assertEquals(1, occurrences(builder, token), name + " / " + role.id());
			}
		}
	}

	private static int occurrences(String source, String token) {
		int count = 0;
		for (int index = source.indexOf(token); index >= 0; index = source.indexOf(token, index + token.length())) {
			count++;
		}
		return count;
	}
}
