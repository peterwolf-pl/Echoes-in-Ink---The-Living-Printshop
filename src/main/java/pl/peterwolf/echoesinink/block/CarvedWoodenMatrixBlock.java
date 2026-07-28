package pl.peterwolf.echoesinink.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import pl.peterwolf.echoesinink.EchoesInInk;

/** Carved matrix that can be copied with charcoal rubbing paper. */
public class CarvedWoodenMatrixBlock extends Block {
	public static final MapCodec<CarvedWoodenMatrixBlock> CODEC = simpleCodec(CarvedWoodenMatrixBlock::new);
	public static final Identifier DEFAULT_PATTERN = EchoesInInk.id("matrix_alpha");

	public CarvedWoodenMatrixBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	public Identifier patternId() {
		return DEFAULT_PATTERN;
	}
}
