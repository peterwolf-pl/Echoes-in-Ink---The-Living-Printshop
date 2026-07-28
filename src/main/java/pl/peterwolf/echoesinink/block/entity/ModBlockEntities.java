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

	static {
		ResourceKey<BlockEntityType<?>> key = ResourceKey.create(
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
				ModBlocks.BROKEN_PRESS_FRAME,
				ModBlocks.INK_STAINED_FLOORBOARDS,
				ModBlocks.FADED_WORKSHOP_PLAQUE
			)
		);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, INVESTIGATION);
	}

	private ModBlockEntities() {}

	public static void init() {
		// static registration
	}
}
