package pl.peterwolf.echoesinink.progression;

import java.util.List;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import pl.peterwolf.echoesinink.config.ModConfig;
import pl.peterwolf.echoesinink.item.ModItems;

/** Adds a deterministic physical starter kit when a printshop chest is first opened. */
public final class PrintshopStarterChestService {
	private PrintshopStarterChestService() {}

	public static void init() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (hand != InteractionHand.MAIN_HAND
				|| !(level instanceof ServerLevel serverLevel)
				|| !(player instanceof ServerPlayer serverPlayer)
				|| !(level.getBlockEntity(hit.getBlockPos()) instanceof ChestBlockEntity chest)) {
				return InteractionResult.PASS;
			}
			LegacyWorkshopBinder.WorkshopArea workshop =
				LegacyWorkshopBinder.findWorkshop(serverLevel, hit.getBlockPos());
			if (workshop.confirmedStructure()) {
				grantStarterSupply(serverLevel, serverPlayer, chest, workshop.workshopId());
			}
			return InteractionResult.PASS;
		});
	}

	public static boolean grantStarterSupply(
		ServerLevel level,
		ServerPlayer player,
		ChestBlockEntity chest,
		String workshopId
	) {
		PrintshopProgressionSavedData progression = PrintshopProgressionSavedData.get(level);
		if (!ModConfig.INSTANCE.starterPrintshopGuaranteesFullPress
			|| !progression.starterRewardsAllowed(workshopId)
			|| !progression.claimStarterSupply(workshopId)) {
			return false;
		}

		chest.unpackLootTable(player);
		for (ItemStack stack : starterKit()) {
			if (!insertIntoChest(chest, stack.copy())) {
				ItemStack overflow = stack.copy();
				if (!player.addItem(overflow)) {
					player.drop(overflow, false);
				}
			}
		}
		chest.setChanged();
		player.sendSystemMessage(Component.translatable("progression.echoes_in_ink.starter_chest"));
		return true;
	}

	public static List<ItemStack> starterKit() {
		return List.of(
			new ItemStack(ModItems.WORKSHOP_BROOM),
			new ItemStack(ModItems.PRINTERS_BRUSH),
			new ItemStack(ModItems.MAGNIFYING_LENS),
			new ItemStack(ModItems.PRINTERS_ARCHIVE),
			new ItemStack(ModItems.PRESS_SCREW),
			new ItemStack(ModItems.PRESS_HANDLE),
			new ItemStack(ModItems.PRESS_PLATEN),
			new ItemStack(ModItems.PRESS_CARRIAGE)
		);
	}

	private static boolean insertIntoChest(ChestBlockEntity chest, ItemStack stack) {
		for (int slot = 0; slot < chest.getContainerSize(); slot++) {
			if (chest.getItem(slot).isEmpty()) {
				chest.setItem(slot, stack);
				return true;
			}
		}
		return false;
	}
}
