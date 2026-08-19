package pl.peterwolf.echoesinink;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import pl.peterwolf.echoesinink.archive.ModAttachments;
import pl.peterwolf.echoesinink.block.ModBlocks;
import pl.peterwolf.echoesinink.block.entity.ModBlockEntities;
import pl.peterwolf.echoesinink.command.EchoesCommands;
import pl.peterwolf.echoesinink.config.ModConfig;
import pl.peterwolf.echoesinink.echo.EchoManager;
import pl.peterwolf.echoesinink.item.ModCreativeTabs;
import pl.peterwolf.echoesinink.item.ModDataComponents;
import pl.peterwolf.echoesinink.item.ModItems;
import pl.peterwolf.echoesinink.networking.EchoPayloads;
import pl.peterwolf.echoesinink.recipe.PrintingRecipes;
import pl.peterwolf.echoesinink.progression.PrintshopStarterChestService;
import pl.peterwolf.echoesinink.sound.ModSounds;
import pl.peterwolf.echoesinink.structure.ModStructures;

/**
 * Common (client + dedicated server) entry point for Echoes in Ink.
 * Gameplay logic lives here and in shared packages — never in client-only classes.
 */
public final class EchoesInInk implements ModInitializer {
	public static final String MOD_ID = "echoes_in_ink";
	public static final String MOD_NAME = "Echoes in Ink";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModConfig.load();

		ModDataComponents.init();
		ModAttachments.init();
		ModSounds.init();
		ModBlocks.init();
		ModBlockEntities.init();
		ModItems.init();
		ModCreativeTabs.init();
		PrintingRecipes.init();
		ModStructures.register();
		PrintshopStarterChestService.init();
		EchoPayloads.registerCommon();
		EchoPayloads.registerServerReceivers();
		EchoManager.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			EchoesCommands.register(dispatcher));

		// Mid-join players receive active echo state near them.
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
			server.execute(() -> EchoManager.syncPlayer(handler.player))
		);

		LOGGER.info("{} v{} initialized (Minecraft 26.2 / Fabric).", MOD_NAME, getModVersion());
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static String getModVersion() {
		return net.fabricmc.loader.api.FabricLoader.getInstance()
			.getModContainer(MOD_ID)
			.map(c -> c.getMetadata().getVersion().getFriendlyString())
			.orElse("unknown");
	}
}
