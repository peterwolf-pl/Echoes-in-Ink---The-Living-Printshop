package pl.peterwolf.echoesinink.progression;

import java.util.ArrayList;
import java.util.List;

/**
 * Testable contract shared by all procedural layouts. The world generator must
 * place every role listed here. Layout-specific decorative geometry is built by
 * {@code AbandonedPrintshopPiece}.
 */
public final class WorkshopLayoutPlan {
	private static final List<InvestigationRole> MANDATORY = List.of(
		InvestigationRole.PRESS_FRAME,
		InvestigationRole.MACHINE_REMAINS,
		InvestigationRole.CELLAR_CACHE,
		InvestigationRole.FLOOR_CACHE,
		InvestigationRole.MATRIX_BENCH,
		InvestigationRole.ARCHIVE_DESK,
		InvestigationRole.INK_STATION,
		InvestigationRole.PLAQUE_CLUE
	);

	private WorkshopLayoutPlan() {}

	public static List<InvestigationRole> roles(WorkshopVariant variant, int layoutIndex) {
		int suspiciousCount = suspiciousFloorCount(variant, layoutIndex);
		return roles(variant, layoutIndex, suspiciousCount);
	}

	public static List<InvestigationRole> roles(WorkshopVariant variant, int layoutIndex, int configuredCount) {
		int suspiciousCount = suspiciousFloorCount(variant, layoutIndex, configuredCount);
		List<InvestigationRole> roles = new ArrayList<>(MANDATORY);
		for (int i = 1; i < suspiciousCount; i++) {
			roles.add(InvestigationRole.SUSPICIOUS_FLOOR);
		}
		return List.copyOf(roles);
	}

	public static int suspiciousFloorCount(WorkshopVariant variant, int layoutIndex) {
		int normalized = Math.floorMod(layoutIndex, 2);
		return switch (variant) {
			case RURAL_WOODCUT -> normalized == 0 ? 3 : 4;
			case TOWN_TYPE_FOUNDRY -> normalized == 0 ? 4 : 5;
			case SCHOLARLY_ARCHIVE -> normalized == 0 ? 3 : 4;
			case BURNED_CLANDESTINE -> normalized == 0 ? 5 : 4;
		};
	}

	public static int suspiciousFloorCount(WorkshopVariant variant, int layoutIndex, int configuredCount) {
		return Math.max(3, Math.min(5, configuredCount));
	}
}
