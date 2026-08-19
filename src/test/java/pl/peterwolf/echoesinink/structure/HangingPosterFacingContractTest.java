package pl.peterwolf.echoesinink.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.junit.jupiter.api.Test;

class HangingPosterFacingContractTest {
	@Test
	void invertPieceFacingUndoesStructurePlaceBlockRemapping() {
		for (Mirror mirror : new Mirror[] {Mirror.NONE, Mirror.LEFT_RIGHT}) {
			for (Rotation rotation : new Rotation[] {
				Rotation.NONE, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180, Rotation.COUNTERCLOCKWISE_90
			}) {
				for (Direction worldFacing : Direction.Plane.HORIZONTAL) {
					Direction local = PosterFacing.invertPieceFacing(worldFacing, mirror, rotation);
					Direction placed = local;
					if (mirror != Mirror.NONE) {
						placed = mirror.getRotation(placed).rotate(placed);
					}
					if (rotation != Rotation.NONE) {
						placed = rotation.rotate(placed);
					}
					assertEquals(worldFacing, placed, mirror + " then " + rotation + " should keep " + worldFacing);
				}
			}
		}
	}
}
