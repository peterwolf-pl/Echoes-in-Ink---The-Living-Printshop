package pl.peterwolf.echoesinink.progression;

import java.util.List;
import java.util.Optional;

/** Data-driven crafting contracts for recoverable multi-part printing forms. */
public final class MatrixAssemblyDefinitions {
	public static final MatrixAssemblyDefinition VILLAGE_CHRONICLE = new MatrixAssemblyDefinition(
		"village_chronicle",
		List.of("upper_matrix_fragment", "lower_matrix_fragment", "missing_letter_insert"),
		"village_chronicle_matrix",
		List.of("village_chronicle_print")
	);

	public static final MatrixAssemblyDefinition FORBIDDEN_NOTICE = new MatrixAssemblyDefinition(
		"forbidden_notice",
		List.of("lead_type_set", "iron_chase", "missing_headline_type", "printers_notes"),
		"forbidden_notice_forme",
		List.of("forbidden_notice_print")
	);

	private static final List<MatrixAssemblyDefinition> ALL = List.of(
		VILLAGE_CHRONICLE,
		FORBIDDEN_NOTICE
	);

	private MatrixAssemblyDefinitions() {}

	public static List<MatrixAssemblyDefinition> all() {
		return ALL;
	}

	public static Optional<MatrixAssemblyDefinition> byResult(String resultId) {
		return ALL.stream().filter(definition -> definition.result().equals(resultId)).findFirst();
	}
}
