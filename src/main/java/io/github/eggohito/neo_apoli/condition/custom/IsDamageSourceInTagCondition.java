package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.DamageSourceContextParameter;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.RegistryUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

import java.util.Set;

public record IsDamageSourceInTagCondition(TagKey<DamageType> tag, DamageSourceContextParameter damageSource) implements Condition {

	public static final MapCodec<IsDamageSourceInTagCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		TagKey.hashedCodec(Registries.DAMAGE_TYPE).fieldOf("tag").forGetter(IsDamageSourceInTagCondition::tag),
		DamageSourceContextParameter.CODEC.fieldOf("damage_source").forGetter(IsDamageSourceInTagCondition::damageSource)
	).apply(instance, IsDamageSourceInTagCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IsDamageSourceInTagCondition> STREAM_CODEC = StreamCodec.composite(
		TagKey.streamCodec(Registries.DAMAGE_TYPE), IsDamageSourceInTagCondition::tag,
		DamageSourceContextParameter.STREAM_CODEC, IsDamageSourceInTagCondition::damageSource,
		IsDamageSourceInTagCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_DAMAGE_SOURCE_IN_TAG;
	}

	@Override
	public boolean test(Context context) {
		return context.getOptional(damageSource())
			.map(source -> source.is(this.tag()))
			.orElse(false);
	}

	@Override
	public Set<Context.Parameter<?>> getRequiredParameters() {
		return Set.of(damageSource());
	}

	@Override
	public void validate(Context.Validator validator) {
		Condition.super.validate(validator);
		RegistryUtil.validateTag(validator.forChild(".tag"), this.tag());
	}

}
