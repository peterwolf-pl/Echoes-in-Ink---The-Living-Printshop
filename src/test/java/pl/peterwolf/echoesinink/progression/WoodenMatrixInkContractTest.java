package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class WoodenMatrixInkContractTest {
	@Test
	void woodenPressMatricesDrawRaisedInkCapsLikeMetalType() throws IOException {
		String renderer = Files.readString(Path.of(
			"src/client/java/pl/peterwolf/echoesinink/client/render/PrintingPressRenderer.java"
		));
		assertTrue(renderer.contains("submitWoodenMatrix("));
		assertTrue(renderer.contains("WOODEN_MATRIX_INK_CAPS"));
		assertTrue(renderer.contains("submitInkCaps("));
		assertTrue(renderer.contains("METAL_TYPE_CAPS"));
		assertFalse(renderer.contains("WOODEN_MATRIX_INK_RIDGES"));
	}
}
