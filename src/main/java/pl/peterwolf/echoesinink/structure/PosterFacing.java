package pl.peterwolf.echoesinink.structure;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

/**
 * StructurePiece.placeBlock remaps FACING with piece mirror then rotation.
 * Invert that so a resolved world facing survives placement.
 */
final class PosterFacing {
	private PosterFacing() {
	}

	static Direction invertPieceFacing(Direction worldFacing, Mirror mirror, Rotation rotation) {
		Direction local = worldFacing;
		if (rotation != Rotation.NONE) {
			local = inverseRotation(rotation).rotate(local);
		}
		if (mirror != Mirror.NONE) {
			local = mirror.getRotation(local).rotate(local);
		}
		return local;
	}

	private static Rotation inverseRotation(Rotation rotation) {
		return switch (rotation) {
			case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
			case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
			case CLOCKWISE_180 -> Rotation.CLOCKWISE_180;
			case NONE -> Rotation.NONE;
		};
	}
}
