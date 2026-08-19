package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import pl.peterwolf.echoesinink.block.PosterKind;

class PrintshopAtmosphereContractTest {
	@Test
	void everyPosterKindHasADistinctBlockTexture() throws IOException {
		Path blockTextures = Path.of("src/main/resources/assets/echoes_in_ink/textures/block");
		assertEquals(6, PosterKind.values().length);
		for (PosterKind kind : PosterKind.values()) {
			String file = kind == PosterKind.WARNING
				? "hanging_poster.png"
				: "hanging_poster_" + kind.getSerializedName() + ".png";
			assertTrue(Files.isRegularFile(blockTextures.resolve(file)), file);
		}
	}

	@Test
	void everyProceduralBuilderHangsSeveralDifferentPostersAndLaysExtraPaper() throws IOException {
		String source = Files.readString(Path.of(
			"src/main/java/pl/peterwolf/echoesinink/structure/AbandonedPrintshopPiece.java"
		));
		for (String name : List.of("Rural", "Town", "Scholarly", "Burned")) {
			int start = source.indexOf("private void build" + name + "(");
			int end = name.equals("Burned")
				? source.indexOf("private void prepareSite(")
				: source.indexOf("private void build", start + 1);
			assertTrue(start >= 0 && end > start, name);
			String builder = source.substring(start, end);
			assertTrue(count(builder, "placeHangingPrint(") >= 10, name + " posters");
			assertTrue(count(builder, "placeLaidPrint(") >= 5, name + " laid papers");
			assertTrue(count(builder, "DUSTY_PRINTING_TABLE") >= 2, name + " tables");
		}
		assertTrue(source.contains("posterWorldFacingOnIntendedWall"));
		assertTrue(source.contains("PosterFacing.invertPieceFacing"));
	}

	private static int count(String source, String token) {
		int total = 0;
		for (int index = source.indexOf(token); index >= 0; index = source.indexOf(token, index + token.length())) {
			total++;
		}
		return total;
	}
}
