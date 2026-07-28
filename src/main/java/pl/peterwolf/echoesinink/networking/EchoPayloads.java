package pl.peterwolf.echoesinink.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import pl.peterwolf.echoesinink.EchoesInInk;

/** Packets for historical echo events. */
public final class EchoPayloads {
	private EchoPayloads() {}

	public record EchoStartPayload(String echoId, BlockPos center, int durationTicks, int startTick)
		implements CustomPacketPayload {
		public static final Type<EchoStartPayload> TYPE = new Type<>(EchoesInInk.id("echo_start"));
		public static final StreamCodec<RegistryFriendlyByteBuf, EchoStartPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, EchoStartPayload::echoId,
			BlockPos.STREAM_CODEC, EchoStartPayload::center,
			ByteBufCodecs.VAR_INT, EchoStartPayload::durationTicks,
			ByteBufCodecs.VAR_INT, EchoStartPayload::startTick,
			EchoStartPayload::new
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record EchoSyncPayload(String echoId, int tick, int beatIndex) implements CustomPacketPayload {
		public static final Type<EchoSyncPayload> TYPE = new Type<>(EchoesInInk.id("echo_sync"));
		public static final StreamCodec<RegistryFriendlyByteBuf, EchoSyncPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, EchoSyncPayload::echoId,
			ByteBufCodecs.VAR_INT, EchoSyncPayload::tick,
			ByteBufCodecs.VAR_INT, EchoSyncPayload::beatIndex,
			EchoSyncPayload::new
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record EchoEndPayload(String echoId, boolean completed) implements CustomPacketPayload {
		public static final Type<EchoEndPayload> TYPE = new Type<>(EchoesInInk.id("echo_end"));
		public static final StreamCodec<RegistryFriendlyByteBuf, EchoEndPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, EchoEndPayload::echoId,
			ByteBufCodecs.BOOL, EchoEndPayload::completed,
			EchoEndPayload::new
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** Client → server: skip current echo (only if already seen or debug). */
	public record EchoSkipPayload() implements CustomPacketPayload {
		public static final Type<EchoSkipPayload> TYPE = new Type<>(EchoesInInk.id("echo_skip"));
		public static final StreamCodec<RegistryFriendlyByteBuf, EchoSkipPayload> CODEC =
			StreamCodec.unit(new EchoSkipPayload());

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public static void registerCommon() {
		PayloadTypeRegistry.clientboundPlay().register(EchoStartPayload.TYPE, EchoStartPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(EchoSyncPayload.TYPE, EchoSyncPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(EchoEndPayload.TYPE, EchoEndPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(EchoSkipPayload.TYPE, EchoSkipPayload.CODEC);
	}

	public static void registerServerReceivers() {
		ServerPlayNetworking.registerGlobalReceiver(EchoSkipPayload.TYPE, (payload, context) ->
			context.server().execute(() ->
				pl.peterwolf.echoesinink.echo.EchoManager.trySkip(context.player())
			)
		);
	}

	public static void sendStart(ServerPlayer player, EchoStartPayload payload) {
		ServerPlayNetworking.send(player, payload);
	}

	public static void sendSync(ServerPlayer player, EchoSyncPayload payload) {
		ServerPlayNetworking.send(player, payload);
	}

	public static void sendEnd(ServerPlayer player, EchoEndPayload payload) {
		ServerPlayNetworking.send(player, payload);
	}
}
