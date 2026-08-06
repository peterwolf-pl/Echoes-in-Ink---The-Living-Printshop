package pl.peterwolf.echoesinink.block.entity;

import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.block.ModBlocks;

public final class ModBlockEntities {
	public static final BlockEntityType<InvestigationBlockEntity> INVESTIGATION;
	public static final BlockEntityType<PrintingPressBlockEntity> PRINTING_PRESS;
	public static final BlockEntityType<LaidPaperBlockEntity> LAID_PAPER;

	static {
		ResourceKey<BlockEntityType<?>> invKey = ResourceKey.create(
			Registries.BLOCK_ENTITY_TYPE,
			EchoesInInk.id("investigation")
		);
		INVESTIGATION = new BlockEntityType<>(
			InvestigationBlockEntity::new,
			Set.of(
				ModBlocks.PRINTING_DEBRIS,
				ModBlocks.DUSTY_PRINTING_TABLE,
				ModBlocks.COLLAPSED_TYPE_CABINET,
				ModBlocks.DAMAGED_ARCHIVE_SHELF,
				ModBlocks.PRESS_FRAME,
				ModBlocks.LOOSE_INK_STAINED_FLOORBOARDS,
				ModBlocks.HIDDEN_FLOOR_COMPARTMENT,
				ModBlocks.FADED_WORKSHOP_PLAQUE
			)
		);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, invKey, INVESTIGATION);

		ResourceKey<BlockEntityType<?>> pressKey = ResourceKey.create(
			Registries.BLOCK_ENTITY_TYPE,
			EchoesInInk.id("printing_press")
		);
		PRINTING_PRESS = new BlockEntityType<>(
			PrintingPressBlockEntity::new,
			Set.of(ModBlocks.PRINTING_PRESS)
		);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, pressKey, PRINTING_PRESS);

		ResourceKey<BlockEntityType<?>> laidKey = ResourceKey.create(
			Registries.BLOCK_ENTITY_TYPE,
			EchoesInInk.id("laid_paper")
		);
		LAID_PAPER = new BlockEntityType<>(
			LaidPaperBlockEntity::new,
			Set.of(ModBlocks.LAID_PAPER)
		);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, laidKey, LAID_PAPER);
	}

	private ModBlockEntities() {}

	public static void init() {
		// static registration
	}
}
