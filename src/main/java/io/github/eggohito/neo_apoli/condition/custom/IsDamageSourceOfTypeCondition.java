package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.DamageSourceContextParameter;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.github.eggohito.neo_apoli.util.RegistryUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

import java.util.Set;

public record IsDamageSourceOfTypeCondition(ResourceKey<DamageType> damageType, DamageSourceContextParameter damageSource) implements Condition {

	public static final MapCodec<IsDamageSourceOfTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceKey.codec(Registries.DAMAGE_TYPE).fieldOf("damage_type").forGetter(IsDamageSourceOfTypeCondition::damageType),
		NeoApoliContextParameterTypes.DAMAGE_SOURCE.codec().fieldOf("damage_source").forGetter(IsDamageSourceOfTypeCondition::damageSource)
	).apply(instance, IsDamageSourceOfTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IsDamageSourceOfTypeCondition> STREAM_CODEC = StreamCodec.composite(
		ResourceKey.streamCodec(Registries.DAMAGE_TYPE), IsDamageSourceOfTypeCondition::damageType,
		NeoApoliContextParameterTypes.DAMAGE_SOURCE.streamCodec(), IsDamageSourceOfTypeCondition::damageSource,
		IsDamageSourceOfTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_DAMAGE_SOURCE_OF_TYPE;
	}

	@Override
	public boolean test(Context context) {
		return context.getOptional(damageSource())
			.map(source -> source.is(this.damageType()))
			.orElse(false);
	}

	@Override
	public Set<ContextParameter<?>> getRequiredParameters() {
		return Set.of(damageSource());
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		RegistryUtil.validateKey(validator.forChild(".damage_type"), this.damageType());
	}

}
