package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class EchoGhostAnimationContractTest {
	@Test
	void echoGhostsUseAHumanoidParticleRigInsteadOfSingleSparks() throws IOException {
		String animator = Files.readString(Path.of(
			"src/client/java/pl/peterwolf/echoesinink/client/animation/EchoGhostAnimator.java"
		));
		String effects = Files.readString(Path.of(
			"src/client/java/pl/peterwolf/echoesinink/client/ClientEchoEffects.java"
		));
		String state = Files.readString(Path.of(
			"src/client/java/pl/peterwolf/echoesinink/client/ClientEchoState.java"
		));
		assertTrue(animator.contains("draw("));
		assertTrue(animator.contains("limb("));
		assertTrue(effects.contains("drawPrinter"));
		assertTrue(effects.contains("drawHelper"));
		assertTrue(state.contains("view.tick - tick > 12"));
	}
}
