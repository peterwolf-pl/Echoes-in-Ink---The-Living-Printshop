package pl.peterwolf.echoesinink.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import pl.peterwolf.echoesinink.block.InvestigationLoot;
import pl.peterwolf.echoesinink.block.InvestigationState;
import pl.peterwolf.echoesinink.block.InvestigatableBlock;

/**
 * Persists investigation progress and ensures loot is rolled at most once.
 * Survives chunk unload and server restart via ValueInput/ValueOutput.
 */
public class InvestigationBlockEntity extends BlockEntity {
	private boolean lootGenerated;
	private String lastResultId = "";

	public InvestigationBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.INVESTIGATION, pos, state);
	}

	public boolean isLootGenerated() {
		return lootGenerated;
	}

	public String lastResultId() {
		return lastResultId;
	}

	/**
	 * Server-only: advance cleaning and, when fully investigated, roll loot once.
	 */
	public boolean clean(ServerLevel level, Player player) {
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof InvestigatableBlock block)) {
			return false;
		}
		InvestigationState current = state.getValue(InvestigatableBlock.INVESTIGATION);
		if (!current.canClean()) {
			return false;
		}

		InvestigationState next = current.next();
		level.setBlock(worldPosition, state.setValue(InvestigatableBlock.INVESTIGATION, next), 3);
		setChanged();

		if (next == InvestigationState.FULLY_INVESTIGATED && !lootGenerated) {
			InvestigationLoot.Result result = InvestigationLoot.roll(level, block.lootProfile());
			lootGenerated = true;
			lastResultId = result.id();
			setChanged();

			ItemStack stack = result.createStack();
			if (!stack.isEmpty()) {
				if (!player.getInventory().add(stack)) {
					ItemEntity drop = new ItemEntity(
						level,
						worldPosition.getX() + 0.5,
						worldPosition.getY() + 1.0,
						worldPosition.getZ() + 0.5,
						stack
					);
					drop.setDefaultPickUpDelay();
					level.addFreshEntity(drop);
				}
			}
			player.sendSystemMessage(result.message());
		}
		return true;
	}

	@Override
	protected void saveAdditional(ValueOutput tag) {
		super.saveAdditional(tag);
		tag.putBoolean("LootGenerated", lootGenerated);
		tag.putString("LastResultId", lastResultId == null ? "" : lastResultId);
	}

	@Override
	protected void loadAdditional(ValueInput tag) {
		super.loadAdditional(tag);
		lootGenerated = tag.getBooleanOr("LootGenerated", false);
		lastResultId = tag.getStringOr("LastResultId", "");
	}
}
