package pl.peterwolf.echoesinink.client;

import net.fabricmc.api.ClientModInitializer;
import pl.peterwolf.echoesinink.EchoesInInk;

/**
 * Client-only entry point. Rendering, animations, and client screens only.
 * Never put server gameplay authority here.
 */
public final class EchoesInInkClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EchoesInInk.LOGGER.info("{} client initialized.", EchoesInInk.MOD_NAME);
	}
}
