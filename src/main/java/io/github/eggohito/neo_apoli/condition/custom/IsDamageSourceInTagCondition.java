package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.damage_source.DamageSourceProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.RegistryUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public record IsDamageSourceInTagCondition(TagKey<DamageType> tag, DamageSourceProvider damageSource) implements Condition {

	public static final MapCodec<IsDamageSourceInTagCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		TagKey.hashedCodec(Registries.DAMAGE_TYPE).fieldOf("tag").forGetter(IsDamageSourceInTagCondition::tag),
		DamageSourceProvider.CODEC.fieldOf("damage_source").forGetter(IsDamageSourceInTagCondition::damageSource)
	).apply(instance, IsDamageSourceInTagCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IsDamageSourceInTagCondition> STREAM_CODEC = StreamCodec.composite(
		TagKey.streamCodec(Registries.DAMAGE_TYPE), IsDamageSourceInTagCondition::tag,
		DamageSourceProvider.STREAM_CODEC, IsDamageSourceInTagCondition::damageSource,
		IsDamageSourceInTagCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_DAMAGE_SOURCE_IN_TAG;
	}

	@Override
	public boolean test(Context context) {
		return damageSource().getValue(context.forChild(".damage_source"))
			.map(source -> source.is(tag()))
			.orElse(false);
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		RegistryUtil.validateTag(validator.forChild(".tag"), this.tag());
		damageSource().validate(validator.forChild(".damage_source"));
	}

}
