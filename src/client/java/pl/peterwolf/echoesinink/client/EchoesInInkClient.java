package pl.peterwolf.echoesinink.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.block.entity.ModBlockEntities;
import pl.peterwolf.echoesinink.client.render.LaidPaperRenderer;
import pl.peterwolf.echoesinink.client.render.PrintingPressRenderer;
import pl.peterwolf.echoesinink.config.ModConfig;
import pl.peterwolf.echoesinink.networking.EchoPayloads;

/**
 * Client-only entry point. Rendering, animations, echo presentation only.
 * Never put server gameplay authority here.
 */
public final class EchoesInInkClient implements ClientModInitializer {
	private static int sneakSkipCooldown;

	@Override
	public void onInitializeClient() {
		ModItemTooltips.init();
		BlockEntityRendererRegistry.register(ModBlockEntities.PRINTING_PRESS, PrintingPressRenderer::new);
		BlockEntityRendererRegistry.register(ModBlockEntities.LAID_PAPER, LaidPaperRenderer::new);

		ClientPlayNetworking.registerGlobalReceiver(EchoPayloads.EchoStartPayload.TYPE, (payload, context) ->
			context.client().execute(() ->
				ClientEchoState.start(payload.echoId(), payload.center(), payload.durationTicks(), payload.startTick())
			)
		);
		ClientPlayNetworking.registerGlobalReceiver(EchoPayloads.EchoSyncPayload.TYPE, (payload, context) ->
			context.client().execute(() ->
				ClientEchoState.sync(payload.echoId(), payload.tick(), payload.beatIndex())
			)
		);
		ClientPlayNetworking.registerGlobalReceiver(EchoPayloads.EchoEndPayload.TYPE, (payload, context) ->
			context.client().execute(() -> ClientEchoState.end(payload.echoId()))
		);

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientEchoState.clear());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientEchoState.clientTick();
			ClientEchoEffects.tick();
			handleSkipInput(client);
		});

		EchoesInInk.LOGGER.info("{} client initialized.", EchoesInInk.MOD_NAME);
	}

	/** Sneak for 1s while an echo is active requests skip (after first view). */
	private static void handleSkipInput(Minecraft client) {
		if (sneakSkipCooldown > 0) {
			sneakSkipCooldown--;
		}
		LocalPlayer player = client.player;
		if (player == null || ClientEchoState.active().isEmpty()) {
			return;
		}
		if (!ModConfig.INSTANCE.echoSkippableAfterFirstView) {
			return;
		}
		if (player.isShiftKeyDown() && sneakSkipCooldown == 0) {
			// Require holding sneak briefly: fire every 40 ticks while sneaking
			if (player.tickCount % 40 == 0) {
				ClientPlayNetworking.send(new EchoPayloads.EchoSkipPayload());
				sneakSkipCooldown = 20;
			}
		}
	}
}
