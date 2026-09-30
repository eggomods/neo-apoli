package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.effect.EffectProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;

public record IsEffectOfTypeCondition(Holder<MobEffect> effectType, EffectProvider effect) implements Condition {

	public static final MapCodec<IsEffectOfTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		MobEffect.CODEC.fieldOf("effect_type").forGetter(IsEffectOfTypeCondition::effectType),
		EffectProvider.CODEC.fieldOf("effect").forGetter(IsEffectOfTypeCondition::effect)
	).apply(instance, IsEffectOfTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IsEffectOfTypeCondition> STREAM_CODEC = StreamCodec.composite(
		MobEffect.STREAM_CODEC, IsEffectOfTypeCondition::effectType,
		EffectProvider.STREAM_CODEC, IsEffectOfTypeCondition::effect,
		IsEffectOfTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_EFFECT_OF_TYPE;
	}

	@Override
	public boolean test(Context context) {
		return effect().getValue(context.forChild(".effect"))
			.stream()
			.anyMatch(effect -> effect.is(this.effectType()));
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		effect().validate(validator.forChild(".effect"));
	}

}
