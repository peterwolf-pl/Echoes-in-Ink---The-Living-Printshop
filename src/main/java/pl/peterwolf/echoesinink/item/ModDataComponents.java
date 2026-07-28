package pl.peterwolf.echoesinink.item;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.block.InvestigationData;

/**
 * Custom data components. Only store safe identifiers — never arbitrary executable content.
 */
public final class ModDataComponents {
	/** Pattern id stored on charcoal rubbings (e.g. echoes_in_ink:matrix_alpha). */
	public static final DataComponentType<Identifier> RUBBING_PATTERN = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		EchoesInInk.id("rubbing_pattern"),
		DataComponentType.<Identifier>builder()
			.persistent(Identifier.CODEC)
			.networkSynchronized(Identifier.STREAM_CODEC)
			.build()
	);

	/**
	 * Investigation progress on dropped / carried workshop blocks.
	 * Prevents loot duplication via break → place.
	 */
	public static final DataComponentType<InvestigationData> INVESTIGATION = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		EchoesInInk.id("investigation"),
		DataComponentType.<InvestigationData>builder()
			.persistent(InvestigationData.CODEC)
			.networkSynchronized(InvestigationData.STREAM_CODEC)
			.build()
	);

	private ModDataComponents() {}

	public static void init() {
		// static registration
	}
}
