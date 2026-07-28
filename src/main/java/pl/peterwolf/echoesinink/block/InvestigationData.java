package pl.peterwolf.echoesinink.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Safe item-stack payload so investigation progress cannot be farmed by break/replace.
 * Only stores plain flags/ids — never executable content.
 */
public record InvestigationData(boolean lootGenerated, String lastResultId, String state) {
	public static final InvestigationData DEFAULT = new InvestigationData(false, "", InvestigationState.UNTOUCHED.getSerializedName());

	public static final Codec<InvestigationData> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.BOOL.optionalFieldOf("loot_generated", false).forGetter(InvestigationData::lootGenerated),
			Codec.STRING.optionalFieldOf("last_result", "").forGetter(InvestigationData::lastResultId),
			Codec.STRING.optionalFieldOf("state", InvestigationState.UNTOUCHED.getSerializedName()).forGetter(InvestigationData::state)
		).apply(instance, InvestigationData::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, InvestigationData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL, InvestigationData::lootGenerated,
		ByteBufCodecs.STRING_UTF8, InvestigationData::lastResultId,
		ByteBufCodecs.STRING_UTF8, InvestigationData::state,
		InvestigationData::new
	);

	public InvestigationState investigationState() {
		for (InvestigationState value : InvestigationState.values()) {
			if (value.getSerializedName().equals(state)) {
				return value;
			}
		}
		return InvestigationState.UNTOUCHED;
	}

	public static InvestigationData of(boolean lootGenerated, String lastResultId, InvestigationState state) {
		return new InvestigationData(
			lootGenerated,
			lastResultId == null ? "" : lastResultId,
			state.getSerializedName()
		);
	}
}
