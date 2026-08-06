package pl.peterwolf.echoesinink.item;

import java.util.function.Consumer;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import pl.peterwolf.echoesinink.archive.ArchiveCategory;
import pl.peterwolf.echoesinink.archive.ArchiveEntries;
import pl.peterwolf.echoesinink.archive.ArchiveService;
import pl.peterwolf.echoesinink.archive.PlayerArchive;

/**
 * Printer's Archive — records discovered knowledge only.
 * Opens a chat listing of unlocked entries (server-authoritative; no free creative inventory).
 */
public class PrintersArchiveItem extends Item {
	public PrintersArchiveItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		PlayerArchive archive = ArchiveService.get(serverPlayer);
		serverPlayer.sendSystemMessage(Component.translatable("archive.echoes_in_ink.title")
			.withStyle(ChatFormatting.GOLD));
		serverPlayer.sendSystemMessage(Component.translatable(
			"archive.echoes_in_ink.count",
			archive.count(),
			ArchiveEntries.all().size()
		).withStyle(ChatFormatting.GRAY));

		sendPressChecklist(serverPlayer, archive);
		sendTrackedProgress(serverPlayer, archive);

		boolean any = false;
		for (ArchiveCategory category : ArchiveEntries.categories()) {
			boolean header = false;
			for (ArchiveEntries.Def def : ArchiveEntries.byCategory(category)) {
				if (!archive.has(def)) {
					continue;
				}
				if (!header) {
					serverPlayer.sendSystemMessage(Component.translatable(
						"archive.echoes_in_ink.category." + category.getSerializedName()
					).withStyle(ChatFormatting.YELLOW));
					header = true;
					any = true;
				}
				serverPlayer.sendSystemMessage(
					Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
						.append(Component.translatable(def.titleKey()).withStyle(ChatFormatting.WHITE))
				);
				if (player.isSecondaryUseActive()) {
					serverPlayer.sendSystemMessage(
						Component.literal("   ").append(
							Component.translatable(def.bodyKey()).withStyle(ChatFormatting.GRAY)
						)
					);
				}
			}
		}

		if (!any) {
			serverPlayer.sendSystemMessage(Component.translatable("archive.echoes_in_ink.none")
				.withStyle(ChatFormatting.DARK_GRAY));
		} else if (!player.isSecondaryUseActive()) {
			serverPlayer.sendSystemMessage(Component.translatable("archive.echoes_in_ink.shift_hint")
				.withStyle(ChatFormatting.DARK_GRAY));
		}

		serverPlayer.sendOverlayMessage(Component.translatable("archive.echoes_in_ink.opened", archive.count()));
		return InteractionResult.SUCCESS_SERVER;
	}

	private static void sendPressChecklist(ServerPlayer player, PlayerArchive archive) {
		player.sendSystemMessage(Component.translatable("archive.echoes_in_ink.press.title")
			.withStyle(ChatFormatting.YELLOW));
		int recovered = 0;
		recovered += sendChecklistLine(player, archive.has(ArchiveEntries.PART_SCREW), "item.echoes_in_ink.press_screw");
		recovered += sendChecklistLine(player, archive.has(ArchiveEntries.PART_HANDLE), "item.echoes_in_ink.press_handle");
		recovered += sendChecklistLine(player, archive.has(ArchiveEntries.PART_PLATEN), "item.echoes_in_ink.press_platen");
		recovered += sendChecklistLine(player, archive.has(ArchiveEntries.PART_CARRIAGE), "item.echoes_in_ink.press_carriage");
		player.sendSystemMessage(Component.translatable("archive.echoes_in_ink.press.count", recovered, 4)
			.withStyle(ChatFormatting.GRAY));
	}

	private static int sendChecklistLine(ServerPlayer player, boolean recovered, String itemKey) {
		player.sendSystemMessage(
			Component.literal(recovered ? "[✓] " : "[ ] ")
				.withStyle(recovered ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY)
				.append(Component.translatable(itemKey).withStyle(ChatFormatting.WHITE))
		);
		return recovered ? 1 : 0;
	}

	private static void sendTrackedProgress(ServerPlayer player, PlayerArchive archive) {
		if (!archive.workshopIds().isEmpty()) {
			sendLiteralSet(player, "archive.echoes_in_ink.tracked.workshops", archive.workshopIds());
		}
		if (!archive.workshopVariants().isEmpty()) {
			player.sendSystemMessage(Component.translatable("archive.echoes_in_ink.tracked.variants")
				.withStyle(ChatFormatting.YELLOW));
			for (String id : archive.workshopVariants().stream().sorted().toList()) {
				player.sendSystemMessage(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
					.append(Component.translatable("structure.echoes_in_ink.variant." + id).withStyle(ChatFormatting.WHITE)));
			}
		}
		if (!archive.recoveredMaterials().isEmpty()) {
			sendTranslatedSet(player, "archive.echoes_in_ink.tracked.materials", archive.recoveredMaterials(), "item.echoes_in_ink.");
		}
		if (!archive.availableRecipes().isEmpty()) {
			sendTranslatedSet(player, "archive.echoes_in_ink.tracked.recipes", archive.availableRecipes(), "recipe.echoes_in_ink.");
		}
		if (!archive.printedWorks().isEmpty()) {
			player.sendSystemMessage(Component.translatable("archive.echoes_in_ink.tracked.works")
				.withStyle(ChatFormatting.YELLOW));
			for (String id : archive.printedWorks().stream().sorted().toList()) {
				Component name = ArchiveEntries.byId(id)
					.<Component>map(def -> Component.translatable(def.titleKey()))
					.orElseGet(() -> Component.translatable("item.echoes_in_ink." + id));
				player.sendSystemMessage(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
					.append(name.copy().withStyle(ChatFormatting.WHITE)));
			}
		}
		if (!archive.unresolvedClues().isEmpty()) {
			player.sendSystemMessage(Component.translatable("archive.echoes_in_ink.tracked.clues")
				.withStyle(ChatFormatting.YELLOW));
			for (String id : archive.unresolvedClues().stream().sorted().toList()) {
				Component name = ArchiveEntries.byId(id)
					.<Component>map(def -> Component.translatable(def.titleKey()))
					.orElseGet(() -> Component.literal(id));
				player.sendSystemMessage(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
					.append(name.copy().withStyle(ChatFormatting.WHITE)));
			}
		}
	}

	private static void sendLiteralSet(ServerPlayer player, String headingKey, Set<String> values) {
		player.sendSystemMessage(Component.translatable(headingKey).withStyle(ChatFormatting.YELLOW));
		for (String value : values.stream().sorted().toList()) {
			player.sendSystemMessage(Component.literal(" • " + value).withStyle(ChatFormatting.WHITE));
		}
	}

	private static void sendTranslatedSet(
		ServerPlayer player,
		String headingKey,
		Set<String> values,
		String translationPrefix
	) {
		player.sendSystemMessage(Component.translatable(headingKey).withStyle(ChatFormatting.YELLOW));
		for (String value : values.stream().sorted().toList()) {
			player.sendSystemMessage(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
				.append(Component.translatable(translationPrefix + value).withStyle(ChatFormatting.WHITE)));
		}
	}

	@Override
	public void appendHoverText(
		ItemStack stack,
		TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> tooltip,
		TooltipFlag flag
	) {
		tooltip.accept(Component.translatable("item.echoes_in_ink.printers_archive.desc").withColor(0xAAAAAA));
	}
}
