package pl.peterwolf.echoesinink.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import pl.peterwolf.echoesinink.block.InvestigationData;
import pl.peterwolf.echoesinink.block.InvestigationState;

/** Block item that shows investigation progress when present on the stack. */
public class InvestigatableBlockItem extends BlockItem {
	public InvestigatableBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(
		ItemStack stack,
		TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> tooltip,
		TooltipFlag flag
	) {
		InvestigationData data = stack.get(ModDataComponents.INVESTIGATION);
		if (data == null) {
			return;
		}
		InvestigationState state = data.investigationState();
		if (state == InvestigationState.UNTOUCHED && !data.lootGenerated()) {
			return;
		}
		tooltip.accept(Component.translatable(
			"item.echoes_in_ink.investigation.tooltip",
			state.getSerializedName()
		).withColor(0xAAAAAA));
		if (data.lootGenerated()) {
			tooltip.accept(Component.translatable("item.echoes_in_ink.investigation.loot_done").withColor(0xFFAA00));
		}
	}
}
