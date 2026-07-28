package pl.peterwolf.echoesinink.item;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.block.ModBlocks;

public final class ModCreativeTabs {
	public static final CreativeModeTab MAIN = FabricCreativeModeTab.builder()
		.title(Component.translatable("itemGroup.echoes_in_ink.main"))
		.icon(() -> new ItemStack(ModItems.PRINTERS_BRUSH))
		.displayItems((params, output) -> {
			for (var item : ModItems.all()) {
				output.accept(item);
			}
			for (var item : ModBlocks.blockItems()) {
				output.accept(item);
			}
		})
		.build();

	private ModCreativeTabs() {}

	public static void init() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, EchoesInInk.id("main"), MAIN);
	}
}
