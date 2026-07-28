package pl.peterwolf.echoesinink.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.BlockHitResult;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.block.InvestigatableBlock;
import pl.peterwolf.echoesinink.block.ModBlocks;
import pl.peterwolf.echoesinink.block.entity.InvestigationBlockEntity;
import pl.peterwolf.echoesinink.block.entity.PrintingPressBlockEntity;
import pl.peterwolf.echoesinink.config.ModConfig;
import pl.peterwolf.echoesinink.item.ModItems;
import pl.peterwolf.echoesinink.structure.ModStructures;

/**
 * Development / operator commands. Permission level: game masters (2).
 */
public final class EchoesCommands {
	private static boolean debugEnabled;

	private EchoesCommands() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			Commands.literal("echoesinink")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("give_test_items")
					.executes(EchoesCommands::giveTestItems))
				.then(Commands.literal("assemble_press")
					.executes(EchoesCommands::assemblePress))
				.then(Commands.literal("locate_printshop")
					.executes(EchoesCommands::locatePrintshop))
				.then(Commands.literal("locate_cache")
					.executes(EchoesCommands::locateCache))
				.then(Commands.literal("trigger_echo")
					.executes(EchoesCommands::triggerEcho))
				.then(Commands.literal("reset_archive")
					.executes(EchoesCommands::resetArchive))
				.then(Commands.literal("debug")
					.executes(EchoesCommands::debugStatus)
					.then(Commands.literal("on").executes(ctx -> setDebug(ctx, true)))
					.then(Commands.literal("off").executes(ctx -> setDebug(ctx, false)))
					.then(Commands.literal("reload_config").executes(EchoesCommands::reloadConfig))
					.then(Commands.literal("inspect").executes(EchoesCommands::inspectTarget)))
		);
	}

	private static int giveTestItems(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player;
		try {
			player = ctx.getSource().getPlayerOrException();
		} catch (Exception e) {
			ctx.getSource().sendFailure(Component.translatable("command.echoes_in_ink.player_only"));
			return 0;
		}

		give(player, new ItemStack(ModItems.PRINTERS_BRUSH));
		give(player, new ItemStack(ModItems.MAGNIFYING_LENS));
		give(player, new ItemStack(ModItems.CHARCOAL_RUBBING_PAPER, 8));
		give(player, new ItemStack(ModItems.BLANK_ARCHIVE_PAGE, 8));
		give(player, new ItemStack(ModItems.DAMAGED_ARCHIVE_PAGE));
		give(player, new ItemStack(ModItems.INK_BALL, 4));
		give(player, new ItemStack(ModItems.INK_PAD, 2));
		give(player, new ItemStack(ModItems.WOODEN_PRINTING_MATRIX));
		give(player, new ItemStack(ModItems.METAL_TYPE_PIECE, 16));
		give(player, new ItemStack(ModItems.PRESS_SCREW));
		give(player, new ItemStack(ModItems.PRESS_HANDLE));
		give(player, new ItemStack(ModItems.PRESS_PLATEN));
		give(player, new ItemStack(ModItems.PRESS_CARRIAGE));
		give(player, new ItemStack(ModItems.RESTORED_CHRONICLE_PAGE));
		give(player, new ItemStack(ModItems.PRINTERS_INSTRUCTION_SHEET));
		give(player, new ItemStack(ModItems.WORKSHOP_MAP_FRAGMENT));
		give(player, new ItemStack(ModItems.DECORATIVE_WOODCUT));
		give(player, new ItemStack(ModItems.PRINTED_WARNING_POSTER));
		give(player, new ItemStack(ModBlocks.PRINTING_DEBRIS));
		give(player, new ItemStack(ModBlocks.CARVED_WOODEN_MATRIX));
		give(player, new ItemStack(ModBlocks.DUSTY_PRINTING_TABLE));
		give(player, new ItemStack(ModBlocks.DAMAGED_ARCHIVE_SHELF));
		give(player, new ItemStack(ModBlocks.BROKEN_PRESS_FRAME));
		give(player, new ItemStack(ModBlocks.PRINTING_PRESS));
		give(player, new ItemStack(ModItems.PRINTERS_ARCHIVE));

		ctx.getSource().sendSuccess(
			() -> Component.translatable("command.echoes_in_ink.give_test_items.success"),
			true
		);
		EchoesInInk.LOGGER.info("give_test_items invoked by {}", player.getScoreboardName());
		return 1;
	}

	private static void give(ServerPlayer player, ItemStack stack) {
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false);
		}
	}

	/** Instantly completes press assembly on the looked-at block. */
	private static int assemblePress(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player;
		try {
			player = ctx.getSource().getPlayerOrException();
		} catch (Exception e) {
			ctx.getSource().sendFailure(Component.translatable("command.echoes_in_ink.player_only"));
			return 0;
		}
		if (!(player.pick(8.0D, 0.0F, false) instanceof BlockHitResult hit)) {
			ctx.getSource().sendFailure(Component.translatable("command.echoes_in_ink.assemble_press.fail"));
			return 0;
		}
		if (!(player.level().getBlockEntity(hit.getBlockPos()) instanceof PrintingPressBlockEntity press)) {
			ctx.getSource().sendFailure(Component.translatable("command.echoes_in_ink.assemble_press.fail"));
			return 0;
		}
		press.forceAssemble();
		ctx.getSource().sendSuccess(
			() -> Component.translatable("command.echoes_in_ink.assemble_press.success"),
			true
		);
		player.sendOverlayMessage(press.nextStepMessage());
		return 1;
	}

	private static int locatePrintshop(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		ServerLevel level = source.getLevel();
		BlockPos origin = BlockPos.containing(source.getPosition());

		var structure = level.registryAccess()
			.lookupOrThrow(Registries.STRUCTURE)
			.get(ModStructures.ABANDONED_PRINTSHOP);

		if (structure.isEmpty()) {
			source.sendFailure(Component.translatable("command.echoes_in_ink.locate_printshop.missing"));
			return 0;
		}

		// Search radius in chunks (100 ≈ similar to vanilla locate).
		Pair<BlockPos, Holder<Structure>> result = level.getChunkSource().getGenerator()
			.findNearestMapStructure(level, HolderSet.direct(structure.get()), origin, 100, false);

		if (result == null) {
			source.sendFailure(Component.translatable("command.echoes_in_ink.locate_printshop.failed"));
			return 0;
		}

		BlockPos found = result.getFirst();
		int teleportY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, found.getX(), found.getZ());
		if (teleportY <= level.getMinY()) {
			teleportY = found.getY();
		}
		int distance = Mth.floor(Mth.sqrt((float) origin.distSqr(new BlockPos(found.getX(), origin.getY(), found.getZ()))));
		String tp = "/tp @s " + found.getX() + " " + teleportY + " " + found.getZ();

		Component coordinates = ComponentUtils.wrapInSquareBrackets(
				Component.translatable("chat.coordinates", found.getX(), teleportY, found.getZ())
			)
			.withStyle(style -> style
				.withColor(ChatFormatting.GREEN)
				.withClickEvent(new ClickEvent.RunCommand(tp))
				.withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip")))
			);

		source.sendSuccess(
			() -> Component.translatable("command.echoes_in_ink.locate_printshop.success", coordinates, distance),
			false
		);
		return distance;
	}

	private static int locateCache(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		ServerLevel level = source.getLevel();
		BlockPos origin = BlockPos.containing(source.getPosition());

		var structure = level.registryAccess()
			.lookupOrThrow(Registries.STRUCTURE)
			.get(ModStructures.INK_ARCHIVE_CACHE);

		if (structure.isEmpty()) {
			source.sendFailure(Component.translatable("command.echoes_in_ink.locate_cache.missing"));
			return 0;
		}

		Pair<BlockPos, Holder<Structure>> result = level.getChunkSource().getGenerator()
			.findNearestMapStructure(level, HolderSet.direct(structure.get()), origin, 100, false);

		if (result == null) {
			source.sendFailure(Component.translatable("command.echoes_in_ink.locate_cache.failed"));
			return 0;
		}

		BlockPos found = result.getFirst();
		int teleportY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, found.getX(), found.getZ());
		if (teleportY <= level.getMinY()) {
			teleportY = found.getY();
		}
		int distance = Mth.floor(Mth.sqrt((float) origin.distSqr(new BlockPos(found.getX(), origin.getY(), found.getZ()))));
		String tp = "/tp @s " + found.getX() + " " + teleportY + " " + found.getZ();

		Component coordinates = ComponentUtils.wrapInSquareBrackets(
				Component.translatable("chat.coordinates", found.getX(), teleportY, found.getZ())
			)
			.withStyle(style -> style
				.withColor(ChatFormatting.GREEN)
				.withClickEvent(new ClickEvent.RunCommand(tp))
				.withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip")))
			);

		source.sendSuccess(
			() -> Component.translatable("command.echoes_in_ink.locate_cache.success", coordinates, distance),
			false
		);
		return distance;
	}

	private static int triggerEcho(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player;
		try {
			player = ctx.getSource().getPlayerOrException();
		} catch (Exception e) {
			ctx.getSource().sendFailure(Component.translatable("command.echoes_in_ink.player_only"));
			return 0;
		}
		boolean ok = pl.peterwolf.echoesinink.echo.EchoManager.startLastPrintRun(
			(ServerLevel) player.level(),
			player.blockPosition(),
			player
		);
		if (ok) {
			ctx.getSource().sendSuccess(
				() -> Component.translatable("command.echoes_in_ink.trigger_echo.success"),
				true
			);
			return 1;
		}
		ctx.getSource().sendFailure(Component.translatable("echo.echoes_in_ink.already_active"));
		return 0;
	}

	private static int resetArchive(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player;
		try {
			player = ctx.getSource().getPlayerOrException();
		} catch (Exception e) {
			ctx.getSource().sendFailure(Component.translatable("command.echoes_in_ink.player_only"));
			return 0;
		}
		pl.peterwolf.echoesinink.archive.ArchiveService.reset(player);
		ctx.getSource().sendSuccess(
			() -> Component.translatable("command.echoes_in_ink.reset_archive.success", player.getName()),
			true
		);
		return 1;
	}

	private static int debugStatus(CommandContext<CommandSourceStack> ctx) {
		boolean on = debugEnabled;
		ctx.getSource().sendSuccess(
			() -> Component.translatable(
				on ? "command.echoes_in_ink.debug.on" : "command.echoes_in_ink.debug.off"
			),
			false
		);
		ctx.getSource().sendSuccess(
			() -> Component.literal(
				"mod=" + EchoesInInk.MOD_ID
					+ " version=" + EchoesInInk.getModVersion()
					+ " config=" + ModConfig.path()
					+ " debugLogging=" + ModConfig.INSTANCE.debugLogging
					+ " items=" + ModItems.all().size()
					+ " blocks=" + ModBlocks.all().size()
			),
			false
		);
		return 1;
	}

	private static int setDebug(CommandContext<CommandSourceStack> ctx, boolean enabled) {
		debugEnabled = enabled;
		ModConfig.INSTANCE.debugLogging = enabled;
		ModConfig.save();
		ctx.getSource().sendSuccess(
			() -> Component.translatable(
				enabled ? "command.echoes_in_ink.debug.on" : "command.echoes_in_ink.debug.off"
			),
			true
		);
		return 1;
	}

	private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {
		ModConfig.load();
		ctx.getSource().sendSuccess(
			() -> Component.translatable("command.echoes_in_ink.config_reloaded"),
			true
		);
		return 1;
	}

	/** Inspect investigation state of the block the player is looking at. */
	private static int inspectTarget(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player;
		try {
			player = ctx.getSource().getPlayerOrException();
		} catch (Exception e) {
			ctx.getSource().sendFailure(Component.translatable("command.echoes_in_ink.player_only"));
			return 0;
		}
		if (!(player.pick(8.0D, 0.0F, false) instanceof BlockHitResult hit)) {
			ctx.getSource().sendFailure(Component.literal("Look at a block."));
			return 0;
		}
		BlockPos pos = hit.getBlockPos();
		BlockState state = player.level().getBlockState(pos);
		BlockEntity be = player.level().getBlockEntity(pos);
		String investigation = state.hasProperty(InvestigatableBlock.INVESTIGATION)
			? state.getValue(InvestigatableBlock.INVESTIGATION).getSerializedName()
			: "n/a";
		boolean loot = be instanceof InvestigationBlockEntity inv && inv.isLootGenerated();
		String result = be instanceof InvestigationBlockEntity inv ? inv.lastResultId() : "";
		ctx.getSource().sendSuccess(
			() -> Component.literal(
				"pos=" + pos.toShortString()
					+ " block=" + state.getBlock()
					+ " investigation=" + investigation
					+ " lootGenerated=" + loot
					+ " lastResult=" + result
			),
			false
		);
		return 1;
	}

	public static boolean isDebugEnabled() {
		return debugEnabled || ModConfig.INSTANCE.debugLogging;
	}
}
