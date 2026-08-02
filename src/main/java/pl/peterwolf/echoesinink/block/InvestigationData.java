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
public record InvestigationData(
	boolean lootGenerated,
	String lastResultId,
	String state,
	String workshopId,
	String workshopVariant,
	String investigationRole
) {
	public static final InvestigationData DEFAULT = new InvestigationData(
		false,
		"",
		InvestigationState.UNTOUCHED.getSerializedName(),
		"",
		"",
		""
	);

	public static final Codec<InvestigationData> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.BOOL.optionalFieldOf("loot_generated", false).forGetter(InvestigationData::lootGenerated),
			Codec.STRING.optionalFieldOf("last_result", "").forGetter(InvestigationData::lastResultId),
			Codec.STRING.optionalFieldOf("state", InvestigationState.UNTOUCHED.getSerializedName()).forGetter(InvestigationData::state),
			Codec.STRING.optionalFieldOf("workshop_id", "").forGetter(InvestigationData::workshopId),
			Codec.STRING.optionalFieldOf("workshop_variant", "").forGetter(InvestigationData::workshopVariant),
			Codec.STRING.optionalFieldOf("investigation_role", "").forGetter(InvestigationData::investigationRole)
		).apply(instance, InvestigationData::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, InvestigationData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL, InvestigationData::lootGenerated,
		ByteBufCodecs.STRING_UTF8, InvestigationData::lastResultId,
		ByteBufCodecs.STRING_UTF8, InvestigationData::state,
		ByteBufCodecs.STRING_UTF8, InvestigationData::workshopId,
		ByteBufCodecs.STRING_UTF8, InvestigationData::workshopVariant,
		ByteBufCodecs.STRING_UTF8, InvestigationData::investigationRole,
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
		return of(lootGenerated, lastResultId, state, "", "", "");
	}

	public static InvestigationData of(
		boolean lootGenerated,
		String lastResultId,
		InvestigationState state,
		String workshopId,
		String workshopVariant,
		String investigationRole
	) {
		return new InvestigationData(
			lootGenerated,
			lastResultId == null ? "" : lastResultId,
			state.getSerializedName(),
			workshopId == null ? "" : workshopId,
			workshopVariant == null ? "" : workshopVariant,
			investigationRole == null ? "" : investigationRole
		);
	}
}
