package pl.peterwolf.echoesinink.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blocks that can be cleaned with the Printer's Brush.
 * Cleaning is always server-side.
 */
public interface Investigatable {
	boolean canClean(BlockState state);

	/**
	 * Apply one full clean action. Returns true if the state changed.
	 */
	boolean clean(ServerLevel level, BlockPos pos, BlockState state, Player player);
}
