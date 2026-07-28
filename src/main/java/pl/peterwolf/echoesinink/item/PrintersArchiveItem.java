package pl.peterwolf.echoesinink.item;

import java.util.function.Consumer;
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
