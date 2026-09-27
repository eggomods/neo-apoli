package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.effect.EffectProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.RegistryUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;

public record IsEffectInTagCondition(TagKey<MobEffect> tag, EffectProvider effect) implements Condition {

	public static final MapCodec<IsEffectInTagCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		TagKey.hashedCodec(Registries.MOB_EFFECT).fieldOf("tag").forGetter(IsEffectInTagCondition::tag),
		EffectProvider.CODEC.fieldOf("effect").forGetter(IsEffectInTagCondition::effect)
	).apply(instance, IsEffectInTagCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IsEffectInTagCondition> STREAM_CODEC = StreamCodec.composite(
		TagKey.streamCodec(Registries.MOB_EFFECT), IsEffectInTagCondition::tag,
		EffectProvider.STREAM_CODEC, IsEffectInTagCondition::effect,
		IsEffectInTagCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_EFFECT_IN_TAG;
	}

	@Override
	public boolean test(Context context) {
		return effect().getValue(context.forChild(".effect"))
			.stream()
			.anyMatch(effect -> effect.getEffect().is(this.tag()));
	}

	@Override
	public void validate(Context.Validator validator) {
		Condition.super.validate(validator);
		RegistryUtil.validateTag(validator.forChild(".tag"), tag());
		effect().validate(validator.forChild(".effect"));
	}

}
