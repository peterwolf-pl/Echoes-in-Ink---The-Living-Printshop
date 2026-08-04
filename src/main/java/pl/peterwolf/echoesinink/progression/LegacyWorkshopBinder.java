package pl.peterwolf.echoesinink.progression;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.block.ModBlocks;
import pl.peterwolf.echoesinink.block.entity.InvestigationBlockEntity;
import pl.peterwolf.echoesinink.structure.AbandonedPrintshopPiece;
import pl.peterwolf.echoesinink.structure.ModStructures;

/**
 * Upgrades investigation nodes generated before semantic workshop roles were
 * introduced. The binding runs only when an unbound node is actually cleaned.
 */
public final class LegacyWorkshopBinder {
	private static final List<InvestigationRole> REQUIRED = List.of(
		InvestigationRole.PRESS_FRAME,
		InvestigationRole.MACHINE_REMAINS,
		InvestigationRole.CELLAR_CACHE,
		InvestigationRole.FLOOR_CACHE,
		InvestigationRole.MATRIX_BENCH,
		InvestigationRole.ARCHIVE_DESK,
		InvestigationRole.INK_STATION,
		InvestigationRole.PLAQUE_CLUE
	);

	private LegacyWorkshopBinder() {}

	public static MigrationResult bind(ServerLevel level, BlockPos origin) {
		WorkshopArea area = findWorkshop(level, origin);
		List<Node> nodes = collectNodes(level, area.bounds());
		if (nodes.stream().noneMatch(node -> node.pos().equals(origin))) {
			return MigrationResult.NONE;
		}
		if (!area.confirmedStructure() && !looksLikeLegacyPrintshop(nodes)) {
			return MigrationResult.NONE;
		}

		List<InvestigationRole> roles = planRoles(nodes.stream().map(LegacyWorkshopBinder::kind).toList());
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			InvestigationRole role = roles.get(i);
			node.blockEntity().configureWorkshop(area.workshopId(), area.variant(), role);
		}

		List<InvestigationRole> compensation = new ArrayList<>();
		for (InvestigationRole role : REQUIRED) {
			int index = roles.indexOf(role);
			if (index < 0 || nodes.get(index).blockEntity().isLootGenerated()) {
				compensation.add(role);
			}
		}
		EchoesInInk.LOGGER.info(
			"Migrated legacy printshop {}: nodes={}, compensatedRoles={}",
			area.workshopId(), nodes.size(), compensation
		);
		return new MigrationResult(true, List.copyOf(compensation));
	}

	private static WorkshopArea findWorkshop(ServerLevel level, BlockPos origin) {
		Structure structure = level.registryAccess()
			.lookupOrThrow(Registries.STRUCTURE)
			.getValue(ModStructures.ABANDONED_PRINTSHOP);
		if (structure != null) {
			StructureStart start = level.structureManager().getStructureWithPieceAt(origin, structure);
			if (start != null && start.isValid()) {
				WorkshopVariant variant = WorkshopVariant.RURAL_WOODCUT;
				String workshopId = WorkshopIdentity.idForChunk(start.getChunkPos());
				for (var piece : start.getPieces()) {
					if (piece instanceof AbandonedPrintshopPiece printshop) {
						variant = printshop.variant();
						if (!printshop.workshopId().isBlank()
							&& !printshop.workshopId().equals("printshop_unknown")) {
							workshopId = printshop.workshopId();
						}
						break;
					}
				}
				return new WorkshopArea(start.getBoundingBox().inflatedBy(1, 2, 1), workshopId, variant, true);
			}
		}

		BoundingBox fallback = new BoundingBox(
			origin.getX() - 18, origin.getY() - 6, origin.getZ() - 18,
			origin.getX() + 18, origin.getY() + 8, origin.getZ() + 18
		);
		ChunkPos chunk = new ChunkPos(origin.getX() >> 4, origin.getZ() >> 4);
		return new WorkshopArea(
			fallback,
			"legacy_" + WorkshopIdentity.idForChunk(chunk),
			WorkshopVariant.RURAL_WOODCUT,
			false
		);
	}

	private static List<Node> collectNodes(ServerLevel level, BoundingBox bounds) {
		List<Node> nodes = new ArrayList<>();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
			for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
				cursor.set(x, bounds.minY(), z);
				if (!level.hasChunkAt(cursor)) {
					continue;
				}
				for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
					cursor.set(x, y, z);
					if (level.getBlockEntity(cursor) instanceof InvestigationBlockEntity investigation
						&& investigation.workshopId().isBlank()) {
						nodes.add(new Node(cursor.immutable(), level.getBlockState(cursor).getBlock(), investigation));
					}
				}
			}
		}
		nodes.sort(Comparator.comparingInt((Node node) -> node.pos().getY())
			.thenComparingInt(node -> node.pos().getX())
			.thenComparingInt(node -> node.pos().getZ()));
		return nodes;
	}

	private static boolean looksLikeLegacyPrintshop(List<Node> nodes) {
		boolean hasPress = nodes.stream().anyMatch(node -> node.block() == ModBlocks.BROKEN_PRESS_FRAME);
		boolean hasPlaque = nodes.stream().anyMatch(node -> node.block() == ModBlocks.FADED_WORKSHOP_PLAQUE);
		boolean hasWorkSurface = nodes.stream().anyMatch(node ->
			node.block() == ModBlocks.DUSTY_PRINTING_TABLE
				|| node.block() == ModBlocks.COLLAPSED_TYPE_CABINET
				|| node.block() == ModBlocks.DAMAGED_ARCHIVE_SHELF
		);
		return nodes.size() >= 4 && hasPress && hasPlaque && hasWorkSurface;
	}

	public static List<InvestigationRole> planRoles(List<NodeKind> nodes) {
		List<InvestigationRole> result = new ArrayList<>(Collections.nCopies(
			nodes.size(), InvestigationRole.SUSPICIOUS_FLOOR
		));
		claim(result, nodes, InvestigationRole.PRESS_FRAME, NodeKind.PRESS_FRAME);
		claim(result, nodes, InvestigationRole.MACHINE_REMAINS, NodeKind.PRESS_FRAME, NodeKind.DEBRIS);
		claim(result, nodes, InvestigationRole.MATRIX_BENCH, NodeKind.TABLE);
		claim(result, nodes, InvestigationRole.ARCHIVE_DESK, NodeKind.SHELF);
		claim(result, nodes, InvestigationRole.INK_STATION, NodeKind.CABINET);
		claim(result, nodes, InvestigationRole.PLAQUE_CLUE, NodeKind.PLAQUE);
		claim(result, nodes, InvestigationRole.FLOOR_CACHE, NodeKind.HIDDEN_FLOOR, NodeKind.DEBRIS);
		claim(result, nodes, InvestigationRole.CELLAR_CACHE, NodeKind.DEBRIS, NodeKind.SHELF, NodeKind.CABINET);
		return List.copyOf(result);
	}

	private static void claim(
		List<InvestigationRole> result,
		List<NodeKind> nodes,
		InvestigationRole role,
		NodeKind... preferred
	) {
		int selected = -1;
		for (NodeKind kind : preferred) {
			for (int i = 0; i < nodes.size(); i++) {
				if (result.get(i) == InvestigationRole.SUSPICIOUS_FLOOR && nodes.get(i) == kind) {
					selected = i;
					break;
				}
			}
			if (selected >= 0) {
				break;
			}
		}
		if (selected < 0) {
			for (int i = 0; i < result.size(); i++) {
				if (result.get(i) == InvestigationRole.SUSPICIOUS_FLOOR) {
					selected = i;
					break;
				}
			}
		}
		if (selected >= 0) {
			result.set(selected, role);
		}
	}

	private static NodeKind kind(Node node) {
		if (node.block() == ModBlocks.BROKEN_PRESS_FRAME) {
			return NodeKind.PRESS_FRAME;
		}
		if (node.block() == ModBlocks.PRINTING_DEBRIS) {
			return NodeKind.DEBRIS;
		}
		if (node.block() == ModBlocks.DUSTY_PRINTING_TABLE) {
			return NodeKind.TABLE;
		}
		if (node.block() == ModBlocks.DAMAGED_ARCHIVE_SHELF) {
			return NodeKind.SHELF;
		}
		if (node.block() == ModBlocks.COLLAPSED_TYPE_CABINET) {
			return NodeKind.CABINET;
		}
		if (node.block() == ModBlocks.HIDDEN_FLOOR_COMPARTMENT) {
			return NodeKind.HIDDEN_FLOOR;
		}
		if (node.block() == ModBlocks.FADED_WORKSHOP_PLAQUE) {
			return NodeKind.PLAQUE;
		}
		if (node.block() == ModBlocks.LOOSE_INK_STAINED_FLOORBOARDS) {
			return NodeKind.LOOSE_FLOOR;
		}
		return NodeKind.OTHER;
	}

	public enum NodeKind {
		PRESS_FRAME,
		DEBRIS,
		TABLE,
		SHELF,
		CABINET,
		HIDDEN_FLOOR,
		PLAQUE,
		LOOSE_FLOOR,
		OTHER
	}

	private record Node(BlockPos pos, Block block, InvestigationBlockEntity blockEntity) {}

	private record WorkshopArea(
		BoundingBox bounds,
		String workshopId,
		WorkshopVariant variant,
		boolean confirmedStructure
	) {}

	public record MigrationResult(boolean migrated, List<InvestigationRole> compensationRoles) {
		public static final MigrationResult NONE = new MigrationResult(false, List.of());
	}
}
