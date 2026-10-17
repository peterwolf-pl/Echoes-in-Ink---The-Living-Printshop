package pl.peterwolf.echoesinink.block;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import pl.peterwolf.echoesinink.EchoesInInk;

/** Carved matrix that can be copied with charcoal rubbing paper. */
public class CarvedWoodenMatrixBlock extends Block {
	public static final Identifier DEFAULT_PATTERN = EchoesInInk.id("matrix_alpha");

	public CarvedWoodenMatrixBlock(Properties properties) {
		super(properties);
	}


	public Identifier patternId() {
		return DEFAULT_PATTERN;
	}
}
