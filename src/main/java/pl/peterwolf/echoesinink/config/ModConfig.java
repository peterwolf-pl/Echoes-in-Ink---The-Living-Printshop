package pl.peterwolf.echoesinink.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import pl.peterwolf.echoesinink.EchoesInInk;

/**
 * Human-editable JSON configuration.
 * Defaults are safe for multiplayer; reload is atomic (swap INSTANCE).
 */
public final class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("echoes_in_ink.json");

	public static ModConfig INSTANCE = new ModConfig();

	// ── Investigation (Phase 1–2) ──────────────────────────────────────────
	/** Ticks required to fully clean a debris block with the Printer's Brush. */
	public int brushCleaningDurationTicks = 40;
	/** Durability lost per successful cleaning step (or full clean). */
	public int brushDurabilityCost = 1;
	/** Cooldown in ticks for Magnifying Lens inspection messages. */
	public int lensInspectCooldownTicks = 20;

	// ── Structure generation (Phase 3) ─────────────────────────────────────
	/** Relative weight / spacing hint for abandoned printshop generation. Higher = rarer. */
	public int printshopSpacingChunks = 32;
	public int printshopSeparationChunks = 12;
	public boolean enablePrintshopGeneration = true;
	/** Use all four stable visual/functional variants; false preserves a rural layout. */
	public boolean enablePrintshopVariants = true;
	/** First structure-bound investigation claims one complete deterministic starter set. */
	public boolean starterPrintshopGuaranteesFullPress = true;
	/** Number of consumable Ink Balls guaranteed at the starter ink station. */
	public int starterInkImpressions = 5;
	/** Total loose/hidden floor investigation targets placed per workshop. */
	public int suspiciousFloorboardsPerWorkshop = 4;
	/** Permit a rare, secondary replacement part in deterministic later-ruin rewards. */
	public boolean allowSparePressPartsInLaterRuins = true;

	// ── Printing press (Phase 4) ───────────────────────────────────────────
	public int defaultPrintingDurationTicks = 60;
	public boolean pressRequireFullAssembly = true;

	// ── Echo events (Phase 6) ──────────────────────────────────────────────
	public int echoDurationTicks = 600; // ~30 seconds
	public boolean echoSkippableAfterFirstView = true;
	public boolean echoSubtitles = true;
	public boolean echoReducedParticles = false;
	public boolean echoReducedFlashes = false;
	public boolean echoReducedScreenTint = false;
	public double echoVolume = 1.0;

	// ── Debug ──────────────────────────────────────────────────────────────
	public boolean debugLogging = false;

	private ModConfig() {}

	public static void load() {
		try {
			if (Files.notExists(PATH)) {
				sanitize(INSTANCE);
				save(INSTANCE);
				EchoesInInk.LOGGER.info("Created default config at {}", PATH);
				return;
			}
			ModConfig loaded = GSON.fromJson(Files.readString(PATH), ModConfig.class);
			if (loaded != null) {
				sanitize(loaded);
				INSTANCE = loaded;
			} else {
				sanitize(INSTANCE);
			}
			EchoesInInk.LOGGER.info("Loaded config from {}", PATH);
		} catch (Exception e) {
			EchoesInInk.LOGGER.error("Could not load config; using defaults", e);
			INSTANCE = new ModConfig();
			sanitize(INSTANCE);
		}
	}

	/**
	 * Clamp out-of-range values so a broken config cannot freeze the server
	 * (e.g. zero durations, absurd echo lengths, negative durability).
	 */
	static void sanitize(ModConfig c) {
		c.brushCleaningDurationTicks = clamp(c.brushCleaningDurationTicks, 5, 400);
		c.brushDurabilityCost = clamp(c.brushDurabilityCost, 0, 64);
		c.lensInspectCooldownTicks = clamp(c.lensInspectCooldownTicks, 0, 200);
		c.printshopSpacingChunks = clamp(c.printshopSpacingChunks, 8, 256);
		c.printshopSeparationChunks = clamp(c.printshopSeparationChunks, 0, Math.max(0, c.printshopSpacingChunks - 1));
		c.starterInkImpressions = clamp(c.starterInkImpressions, 1, 16);
		c.suspiciousFloorboardsPerWorkshop = clamp(c.suspiciousFloorboardsPerWorkshop, 3, 5);
		c.defaultPrintingDurationTicks = clamp(c.defaultPrintingDurationTicks, 10, 600);
		c.echoDurationTicks = clamp(c.echoDurationTicks, 100, 72000);
		if (Double.isNaN(c.echoVolume) || Double.isInfinite(c.echoVolume)) {
			c.echoVolume = 1.0;
		}
		c.echoVolume = Math.max(0.0, Math.min(2.0, c.echoVolume));
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	public static void save() {
		try {
			save(INSTANCE);
		} catch (IOException e) {
			EchoesInInk.LOGGER.error("Could not save config", e);
		}
	}

	private static void save(ModConfig config) throws IOException {
		Files.createDirectories(PATH.getParent());
		Files.writeString(PATH, GSON.toJson(config));
	}

	public static Path path() {
		return PATH;
	}
}
