package pl.peterwolf.echoesinink.archive;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * All archive entry definitions. Missing entries stay hidden until unlocked.
 */
public final class ArchiveEntries {
	public record Def(String id, ArchiveCategory category, String titleKey, String bodyKey) {}

	public static final Def WORKSHOP_ASHEN = entry("workshop_ashen", ArchiveCategory.WORKSHOPS);
	public static final Def PART_SCREW = entry("part_screw", ArchiveCategory.MACHINE_PARTS);
	public static final Def PART_HANDLE = entry("part_handle", ArchiveCategory.MACHINE_PARTS);
	public static final Def PART_PLATEN = entry("part_platen", ArchiveCategory.MACHINE_PARTS);
	public static final Def PART_CARRIAGE = entry("part_carriage", ArchiveCategory.MACHINE_PARTS);
	public static final Def MATRIX_WOODEN = entry("matrix_wooden", ArchiveCategory.PRINTING_MATRICES);
	public static final Def MATRIX_TYPE = entry("matrix_type", ArchiveCategory.PRINTING_MATRICES);
	public static final Def MATRIX_RUBBING = entry("matrix_rubbing", ArchiveCategory.PRINTING_MATRICES);
	public static final Def WORK_INSTRUCTION = entry("work_instruction", ArchiveCategory.PRINTED_WORKS);
	public static final Def WORK_CHRONICLE = entry("work_chronicle", ArchiveCategory.PRINTED_WORKS);
	public static final Def WORK_WOODCUT = entry("work_woodcut", ArchiveCategory.PRINTED_WORKS);
	public static final Def WORK_POSTER = entry("work_poster", ArchiveCategory.PRINTED_WORKS);
	public static final Def WORK_MAP = entry("work_map", ArchiveCategory.PRINTED_WORKS);
	public static final Def ECHO_LAST_PRINT = entry("echo_last_print", ArchiveCategory.HISTORICAL_ECHOES);
	public static final Def CLUE_DUST = entry("clue_dust", ArchiveCategory.UNRESOLVED_CLUES);
	public static final Def CLUE_HIDDEN = entry("clue_hidden", ArchiveCategory.UNRESOLVED_CLUES);
	public static final Def CLUE_PLAQUE = entry("clue_plaque", ArchiveCategory.UNRESOLVED_CLUES);

	private static final List<Def> ALL = List.of(
		WORKSHOP_ASHEN,
		PART_SCREW, PART_HANDLE, PART_PLATEN, PART_CARRIAGE,
		MATRIX_WOODEN, MATRIX_TYPE, MATRIX_RUBBING,
		WORK_INSTRUCTION, WORK_CHRONICLE, WORK_WOODCUT, WORK_POSTER, WORK_MAP,
		ECHO_LAST_PRINT,
		CLUE_DUST, CLUE_HIDDEN, CLUE_PLAQUE
	);

	private ArchiveEntries() {}

	private static Def entry(String id, ArchiveCategory category) {
		return new Def(
			id,
			category,
			"archive.echoes_in_ink.entry." + id + ".title",
			"archive.echoes_in_ink.entry." + id + ".body"
		);
	}

	public static List<Def> all() {
		return ALL;
	}

	public static List<Def> byCategory(ArchiveCategory category) {
		return ALL.stream().filter(d -> d.category() == category).toList();
	}

	public static Optional<Def> byId(String id) {
		return ALL.stream().filter(d -> d.id().equals(id)).findFirst();
	}

	public static List<ArchiveCategory> categories() {
		return Arrays.asList(ArchiveCategory.values());
	}
}
