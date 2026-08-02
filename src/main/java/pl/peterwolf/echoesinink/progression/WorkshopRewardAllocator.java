package pl.peterwolf.echoesinink.progression;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure deterministic progression allocator.
 *
 * <p>The first investigated workshop is claimed by world saved data. Its
 * semantic locations always yield the complete starter inventory. Later
 * workshops use the same stable workshop id and role to select specialist
 * rewards; optional randomness is reserved for ordinary chests and unbound
 * debris.</p>
 */
public final class WorkshopRewardAllocator {
	private WorkshopRewardAllocator() {}

	public static List<RewardStack> starter(InvestigationRole role, int inkImpressions) {
		int ink = Math.max(1, inkImpressions);
		return switch (role) {
			case PRESS_FRAME -> rewards(RewardKind.PRESS_SCREW, 1);
			case MACHINE_REMAINS -> rewards(RewardKind.PRESS_PLATEN, 1);
			case CELLAR_CACHE -> rewards(
				RewardKind.PRESS_CARRIAGE, 1,
				RewardKind.DAMAGED_PAGE, 1
			);
			case FLOOR_CACHE -> rewards(RewardKind.PRESS_HANDLE, 1);
			case MATRIX_BENCH -> rewards(RewardKind.WOODEN_MATRIX, 1);
			case ARCHIVE_DESK -> rewards(
				RewardKind.DAMAGED_PAGE, 1,
				RewardKind.BLANK_PAGE, Math.max(5, ink)
			);
			case INK_STATION -> rewards(RewardKind.INK_BALL, ink);
			case PLAQUE_CLUE -> rewards(
				RewardKind.INSTRUCTION_SHEET, 1,
				RewardKind.MAP_FRAGMENT, 1
			);
			case SUSPICIOUS_FLOOR -> List.of();
		};
	}

	public static List<RewardStack> later(
		WorkshopVariant variant,
		InvestigationRole role,
		String workshopId,
		boolean allowSparePressParts
	) {
		long seed = stableSeed(workshopId + ":" + variant.id() + ":" + role.id());
		List<RewardStack> rewards = new ArrayList<>(specialistRewards(variant, role, seed));
		// A spare is deliberately rare, stable, and always secondary to a specialist reward.
		if (allowSparePressParts && role == InvestigationRole.MACHINE_REMAINS && Math.floorMod(seed, 12L) == 0L) {
			RewardKind[] parts = {
				RewardKind.PRESS_SCREW,
				RewardKind.PRESS_HANDLE,
				RewardKind.PRESS_PLATEN,
				RewardKind.PRESS_CARRIAGE
			};
			rewards.add(new RewardStack(parts[(int) Math.floorMod(seed >>> 8, parts.length)], 1));
		}
		return List.copyOf(rewards);
	}

	private static List<RewardStack> specialistRewards(WorkshopVariant variant, InvestigationRole role, long seed) {
		int small = 1 + (int) Math.floorMod(seed, 3L);
		return switch (variant) {
			case RURAL_WOODCUT -> switch (role) {
				case PRESS_FRAME, MATRIX_BENCH -> rewards(RewardKind.UPPER_MATRIX_FRAGMENT, 1);
				case MACHINE_REMAINS, FLOOR_CACHE -> rewards(RewardKind.LOWER_MATRIX_FRAGMENT, 1);
				case CELLAR_CACHE -> rewards(RewardKind.MISSING_LETTER_INSERT, 1, RewardKind.PRINTERS_NOTES, 1);
				case ARCHIVE_DESK -> rewards(RewardKind.DAMAGED_PAGE, small, RewardKind.BLANK_PAGE, 2 + small);
				case INK_STATION -> rewards(RewardKind.INK_BALL, 4 + small);
				case PLAQUE_CLUE -> rewards(RewardKind.MAP_FRAGMENT, 1);
				case SUSPICIOUS_FLOOR -> rewards(RewardKind.CHARCOAL_RUBBING_PAPER, small);
			};
			case TOWN_TYPE_FOUNDRY -> switch (role) {
				case PRESS_FRAME, MACHINE_REMAINS -> rewards(RewardKind.IRON_CHASE, 1);
				case CELLAR_CACHE -> rewards(RewardKind.MISSING_HEADLINE_TYPE, 1, RewardKind.PRINTERS_NOTES, 1);
				case FLOOR_CACHE, MATRIX_BENCH -> rewards(RewardKind.LEAD_TYPE_SET, 1);
				case ARCHIVE_DESK -> rewards(RewardKind.PRINTERS_NOTES, 1, RewardKind.BLANK_PAGE, 2 + small);
				case INK_STATION -> rewards(RewardKind.INK_PAD, 4 + small);
				case PLAQUE_CLUE -> rewards(RewardKind.MAP_FRAGMENT, 1);
				case SUSPICIOUS_FLOOR -> rewards(RewardKind.METAL_TYPE, small);
			};
			case SCHOLARLY_ARCHIVE -> switch (role) {
				case PRESS_FRAME, MATRIX_BENCH -> rewards(RewardKind.LOWER_MATRIX_FRAGMENT, 1);
				case MACHINE_REMAINS -> rewards(RewardKind.MISSING_LETTER_INSERT, 1);
				case CELLAR_CACHE -> rewards(RewardKind.PRINTERS_NOTES, 1, RewardKind.DAMAGED_PAGE, 2);
				case FLOOR_CACHE -> rewards(RewardKind.UPPER_MATRIX_FRAGMENT, 1);
				case ARCHIVE_DESK -> rewards(RewardKind.DAMAGED_PAGE, 2 + small, RewardKind.BLANK_PAGE, 3 + small);
				case INK_STATION -> rewards(RewardKind.INK_BALL, 4 + small);
				case PLAQUE_CLUE -> rewards(RewardKind.MAP_FRAGMENT, 1, RewardKind.PRINTERS_NOTES, 1);
				case SUSPICIOUS_FLOOR -> rewards(RewardKind.DAMAGED_PAGE, 1);
			};
			case BURNED_CLANDESTINE -> switch (role) {
				case PRESS_FRAME, FLOOR_CACHE -> rewards(RewardKind.MISSING_HEADLINE_TYPE, 1);
				case MACHINE_REMAINS -> rewards(RewardKind.LEAD_TYPE_SET, 1);
				case CELLAR_CACHE -> rewards(RewardKind.IRON_CHASE, 1, RewardKind.PRINTERS_NOTES, 1);
				case MATRIX_BENCH -> rewards(RewardKind.MISSING_LETTER_INSERT, 1);
				case ARCHIVE_DESK -> rewards(RewardKind.DAMAGED_PAGE, small, RewardKind.PRINTERS_NOTES, 1);
				case INK_STATION -> rewards(RewardKind.INK_PAD, 4 + small);
				case PLAQUE_CLUE -> rewards(RewardKind.MAP_FRAGMENT, 1);
				case SUSPICIOUS_FLOOR -> rewards(RewardKind.CHARCOAL_RUBBING_PAPER, 1 + small);
			};
		};
	}

	private static List<RewardStack> rewards(Object... values) {
		if ((values.length & 1) != 0) {
			throw new IllegalArgumentException("Rewards require kind/count pairs");
		}
		List<RewardStack> result = new ArrayList<>(values.length / 2);
		for (int i = 0; i < values.length; i += 2) {
			result.add(new RewardStack((RewardKind) values[i], (Integer) values[i + 1]));
		}
		return List.copyOf(result);
	}

	public static long stableSeed(String value) {
		long hash = 0xcbf29ce484222325L;
		String safe = value == null ? "" : value;
		for (int i = 0; i < safe.length(); i++) {
			hash ^= safe.charAt(i);
			hash *= 0x100000001b3L;
		}
		return hash;
	}
}
