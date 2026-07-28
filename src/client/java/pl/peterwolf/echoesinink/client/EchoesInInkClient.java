package pl.peterwolf.echoesinink.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.block.entity.ModBlockEntities;
import pl.peterwolf.echoesinink.client.render.PrintingPressRenderer;

/**
 * Client-only entry point. Rendering and animations only.
 * Never put server gameplay authority here.
 */
public final class EchoesInInkClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.register(ModBlockEntities.PRINTING_PRESS, PrintingPressRenderer::new);
		EchoesInInk.LOGGER.info("{} client initialized.", EchoesInInk.MOD_NAME);
	}
}
