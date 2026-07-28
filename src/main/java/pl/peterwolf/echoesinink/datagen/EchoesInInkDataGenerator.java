package pl.peterwolf.echoesinink.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import pl.peterwolf.echoesinink.EchoesInInk;

/**
 * Fabric data generation entrypoint.
 * Phase 0 registers the generator shell; later phases add language, models, loot, tags, recipes.
 */
public final class EchoesInInkDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();
		// Phase 1+: pack.addProvider(...)
		EchoesInInk.LOGGER.info("Data generator pack created (providers registered in later phases).");
	}
}
