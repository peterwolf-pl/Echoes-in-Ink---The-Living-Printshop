package pl.peterwolf.echoesinink.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import pl.peterwolf.echoesinink.EchoesInInk;

/**
 * Custom sound events (implemented as vanilla-event aliases in sounds.json).
 * Gives accessible subtitles without shipping custom audio assets.
 */
public final class ModSounds {
	public static final SoundEvent PRESS_ASSEMBLE = register("block.printing_press.assemble");
	public static final SoundEvent PRESS_LOAD = register("block.printing_press.load");
	public static final SoundEvent PRESS_INK = register("block.printing_press.ink");
	public static final SoundEvent PRESS_CARRIAGE = register("block.printing_press.carriage");
	public static final SoundEvent PRESS_WORK = register("block.printing_press.work");
	public static final SoundEvent PRESS_IMPRESSION = register("block.printing_press.impression");
	public static final SoundEvent PRESS_COLLECT = register("block.printing_press.collect");
	public static final SoundEvent CHRONICLE_READ = register("item.chronicle.read");
	public static final SoundEvent ARCHIVE_UNLOCK = register("item.archive.unlock");
	public static final SoundEvent ECHO_AMBIENT = register("echo.ambient");
	public static final SoundEvent ECHO_IMPACT = register("echo.impact");
	public static final SoundEvent ECHO_CLUE = register("echo.clue");

	private ModSounds() {}

	public static void init() {
		// static registration
	}

	private static SoundEvent register(String path) {
		Identifier id = EchoesInInk.id(path);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}
}
