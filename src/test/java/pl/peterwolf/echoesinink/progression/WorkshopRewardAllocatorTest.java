package pl.peterwolf.echoesinink.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WorkshopRewardAllocatorTest {
	@Test
	void everyStarterVariantAndLayoutContainsOneCompleteUniquePressSet() {
		for (WorkshopVariant variant : WorkshopVariant.values()) {
			for (int layout = 0; layout < 2; layout++) {
				Map<RewardKind, Integer> totals = totals(starterRewards(variant, layout, 5));
				assertEquals(1, totals.getOrDefault(RewardKind.PRESS_SCREW, 0), label(variant, layout));
				assertEquals(1, totals.getOrDefault(RewardKind.PRESS_HANDLE, 0), label(variant, layout));
				assertEquals(1, totals.getOrDefault(RewardKind.PRESS_PLATEN, 0), label(variant, layout));
				assertEquals(1, totals.getOrDefault(RewardKind.PRESS_CARRIAGE, 0), label(variant, layout));
				int partCount = totals.entrySet().stream()
					.filter(entry -> entry.getKey().isRequiredPressPart())
					.mapToInt(Map.Entry::getValue)
					.sum();
				assertEquals(4, partCount, label(variant, layout));
			}
		}
	}

	@Test
	void starterContainsEverythingNeededForTheFirstPrintLoop() {
		for (WorkshopVariant variant : WorkshopVariant.values()) {
			Map<RewardKind, Integer> totals = totals(starterRewards(variant, 0, 5));
			assertEquals(1, totals.getOrDefault(RewardKind.WOODEN_MATRIX, 0), variant.id());
			assertTrue(totals.getOrDefault(RewardKind.DAMAGED_PAGE, 0) >= 1, variant.id());
			assertTrue(totals.getOrDefault(RewardKind.BLANK_PAGE, 0) >= 5, variant.id());
			assertTrue(totals.getOrDefault(RewardKind.INK_BALL, 0) >= 5, variant.id());
			assertEquals(1, totals.getOrDefault(RewardKind.INSTRUCTION_SHEET, 0), variant.id());
			assertEquals(1, totals.getOrDefault(RewardKind.MAP_FRAGMENT, 0), variant.id());
		}
	}

	@Test
	void laterWorkshopsPrioritizeSpecialistContentAndRemainDeterministic() {
		for (WorkshopVariant variant : WorkshopVariant.values()) {
			int specialistStacks = 0;
			int spareStacks = 0;
			for (InvestigationRole role : WorkshopLayoutPlan.roles(variant, 1)) {
				List<RewardStack> first = WorkshopRewardAllocator.later(
					variant, role, "printshop_test_" + variant.id(), true
				);
				List<RewardStack> second = WorkshopRewardAllocator.later(
					variant, role, "printshop_test_" + variant.id(), true
				);
				assertEquals(first, second, variant.id() + ":" + role.id());
				for (RewardStack reward : first) {
					if (reward.kind().isRequiredPressPart()) {
						spareStacks++;
					} else {
						specialistStacks++;
					}
				}
			}
			assertTrue(specialistStacks > spareStacks, variant.id());
		}
	}

	@Test
	void suspiciousFloorCountIsAlwaysWithinConfiguredDesignRange() {
		for (WorkshopVariant variant : WorkshopVariant.values()) {
			for (int layout = 0; layout < 2; layout++) {
				int count = WorkshopLayoutPlan.suspiciousFloorCount(variant, layout);
				assertTrue(count >= 3 && count <= 5, label(variant, layout));
				long roles = WorkshopLayoutPlan.roles(variant, layout).stream()
					.filter(role -> role == InvestigationRole.FLOOR_CACHE
						|| role == InvestigationRole.SUSPICIOUS_FLOOR)
					.count();
				assertEquals(count, roles, label(variant, layout));
			}
		}
	}

	@Test
	void configuredSuspiciousFloorCountIsConnectedAndSanitizedToThreeThroughFive() {
		for (WorkshopVariant variant : WorkshopVariant.values()) {
			assertEquals(3, WorkshopLayoutPlan.suspiciousFloorCount(variant, 0, -20));
			assertEquals(4, WorkshopLayoutPlan.suspiciousFloorCount(variant, 1, 4));
			assertEquals(5, WorkshopLayoutPlan.suspiciousFloorCount(variant, 0, 50));
			for (int configured = 3; configured <= 5; configured++) {
				long floorRoles = WorkshopLayoutPlan.roles(variant, 0, configured).stream()
					.filter(role -> role == InvestigationRole.FLOOR_CACHE
						|| role == InvestigationRole.SUSPICIOUS_FLOOR)
					.count();
				assertEquals(configured, floorRoles, variant.id());
			}
		}
	}

	@Test
	void changingWorkshopIdentityChangesStableAllocationKey() {
		long original = WorkshopRewardAllocator.stableSeed("printshop_a:press_frame");
		assertEquals(original, WorkshopRewardAllocator.stableSeed("printshop_a:press_frame"));
		assertFalse(original == WorkshopRewardAllocator.stableSeed("printshop_b:press_frame"));
		assertFalse(original == WorkshopRewardAllocator.stableSeed("printshop_a:cellar_cache"));
	}

	private static List<RewardStack> starterRewards(WorkshopVariant variant, int layout, int ink) {
		return WorkshopLayoutPlan.roles(variant, layout).stream()
			.flatMap(role -> WorkshopRewardAllocator.starter(role, ink).stream())
			.toList();
	}

	private static Map<RewardKind, Integer> totals(List<RewardStack> rewards) {
		Map<RewardKind, Integer> totals = new EnumMap<>(RewardKind.class);
		for (RewardStack reward : rewards) {
			totals.merge(reward.kind(), reward.count(), Integer::sum);
		}
		return totals;
	}

	private static String label(WorkshopVariant variant, int layout) {
		return variant.id() + " layout " + layout;
	}
}
