package pl.peterwolf.echoesinink.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import pl.peterwolf.echoesinink.EchoesInInk;

/**
 * All mod items. Registration is static; call {@link #init()} from the common entrypoint.
 */
public final class ModItems {
	private static final List<Item> ALL = new ArrayList<>();

	public static final Item WORKSHOP_BROOM = register("workshop_broom",
		props -> new WorkshopBroomItem(props.durability(96).stacksTo(1)));

	public static final Item PRINTERS_BRUSH = register("printers_brush",
		props -> new PrintersBrushItem(props.durability(64).stacksTo(1)));

	public static final Item MAGNIFYING_LENS = register("magnifying_lens",
		props -> new MagnifyingLensItem(props.stacksTo(1)));

	public static final Item CHARCOAL_RUBBING_PAPER = register("charcoal_rubbing_paper",
		props -> new CharcoalRubbingPaperItem(props.stacksTo(16)));

	public static final Item BLANK_ARCHIVE_PAGE = register("blank_archive_page",
		props -> new PlaceableArchivePageItem(props.stacksTo(64), "item.echoes_in_ink.blank_archive_page.desc"));

	public static final Item DAMAGED_ARCHIVE_PAGE = register("damaged_archive_page",
		props -> new PlaceableArchivePageItem(props.stacksTo(16), "item.echoes_in_ink.damaged_archive_page.desc"));

	public static final Item INK_BALL = register("ink_ball",
		props -> new TooltipItem(props.stacksTo(16), "item.echoes_in_ink.ink_ball.desc"));

	public static final Item INK_PAD = register("ink_pad",
		props -> new TooltipItem(props.stacksTo(16), "item.echoes_in_ink.ink_pad.desc"));

	public static final Item WOODEN_PRINTING_MATRIX = register("wooden_printing_matrix",
		props -> new TooltipItem(props.stacksTo(8), "item.echoes_in_ink.wooden_printing_matrix.desc"));

	public static final Item METAL_TYPE_PIECE = register("metal_type_piece",
		props -> new TooltipItem(props.stacksTo(8), "item.echoes_in_ink.metal_type_piece.desc"));

	public static final Item VILLAGE_CHRONICLE_MATRIX = register("village_chronicle_matrix",
		props -> new TooltipItem(props.stacksTo(8), "item.echoes_in_ink.village_chronicle_matrix.desc"));

	public static final Item LEAD_TYPE_SET = register("lead_type_set",
		props -> new TooltipItem(props.stacksTo(16), "item.echoes_in_ink.lead_type_set.desc"));

	public static final Item IRON_CHASE = register("iron_chase",
		props -> new TooltipItem(props.stacksTo(16), "item.echoes_in_ink.iron_chase.desc"));

	public static final Item MISSING_HEADLINE_TYPE = register("missing_headline_type",
		props -> new TooltipItem(props.stacksTo(16), "item.echoes_in_ink.missing_headline_type.desc"));

	public static final Item PRINTERS_NOTES = register("printers_notes",
		props -> new TooltipItem(props.stacksTo(16), "item.echoes_in_ink.printers_notes.desc"));

	public static final Item FORBIDDEN_NOTICE_FORME = register("forbidden_notice_forme",
		props -> new TooltipItem(props.stacksTo(8), "item.echoes_in_ink.forbidden_notice_forme.desc"));

	public static final Item PRESS_SCREW = register("press_screw",
		props -> new TooltipItem(props.stacksTo(8), "item.echoes_in_ink.press_screw.desc"));

	public static final Item PRESS_HANDLE = register("press_handle",
		props -> new TooltipItem(props.stacksTo(8), "item.echoes_in_ink.press_handle.desc"));

	public static final Item PRESS_PLATEN = register("press_platen",
		props -> new TooltipItem(props.stacksTo(8), "item.echoes_in_ink.press_platen.desc"));

	public static final Item PRESS_CARRIAGE = register("press_carriage",
		props -> new TooltipItem(props.stacksTo(8), "item.echoes_in_ink.press_carriage.desc"));

	public static final Item RESTORED_CHRONICLE_PAGE = register("restored_chronicle_page",
		props -> new RestoredChroniclePageItem(props.stacksTo(16)));

	/** Result of a successful charcoal rubbing (holds pattern component). */
	public static final Item CHARCOAL_RUBBING = register("charcoal_rubbing",
		props -> new CharcoalRubbingItem(props.stacksTo(16)));

	// ── Print outputs (readable; poster is also hangable) ──────────────────
	public static final Item PRINTERS_INSTRUCTION_SHEET = register("printers_instruction_sheet",
		props -> new ReadablePrintItem(props.stacksTo(16), "printers_instruction_sheet", 6));

	public static final Item WORKSHOP_MAP_FRAGMENT = register("workshop_map_fragment",
		props -> new ReadablePrintItem(props.stacksTo(16), "workshop_map_fragment", 4));

	public static final Item DECORATIVE_WOODCUT = register("decorative_woodcut",
		props -> new ReadablePrintItem(props.stacksTo(16), "decorative_woodcut", 3));

	public static final Item PRINTED_WARNING_POSTER = register("printed_warning_poster",
		props -> new PrintedWarningPosterItem(props.stacksTo(16)));

	public static final Item VILLAGE_CHRONICLE_PRINT = register("village_chronicle_print",
		props -> new ReadablePrintItem(props.stacksTo(16), "village_chronicle_print", 5));

	public static final Item FORBIDDEN_NOTICE_PRINT = register("forbidden_notice_print",
		props -> new ReadablePrintItem(props.stacksTo(16), "forbidden_notice_print", 5));

	public static final Item PRINTERS_ARCHIVE = register("printers_archive",
		props -> new PrintersArchiveItem(props.stacksTo(1)));

	private ModItems() {}

	public static void init() {
		// static fields register on class load
	}

	public static List<Item> all() {
		return List.copyOf(ALL);
	}

	private static Item register(String path, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, EchoesInInk.id(path));
		Item item = factory.apply(new Item.Properties().setId(key));
		Registry.register(BuiltInRegistries.ITEM, key, item);
		ALL.add(item);
		return item;
	}
}
