package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public record EffectAmplifierIntProvider(Holder<MobEffect> effect, EntityProvider entity) implements IntProvider {

	public static final MapCodec<EffectAmplifierIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		MobEffect.CODEC.fieldOf("effect").forGetter(EffectAmplifierIntProvider::effect),
		EntityProvider.CODEC.fieldOf("entity").forGetter(EffectAmplifierIntProvider::entity)
	).apply(instance, EffectAmplifierIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EffectAmplifierIntProvider> STREAM_CODEC = StreamCodec.composite(
		MobEffect.STREAM_CODEC, EffectAmplifierIntProvider::effect,
		EntityProvider.STREAM_CODEC, EffectAmplifierIntProvider::entity,
		EffectAmplifierIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.EFFECT_AMPLIFIER;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		Context entityContext = context.forChild(".entity");
		Entity entity = entity().getValue(entityContext).orElse(null);

		switch (entity) {
			case LivingEntity livingEntity when livingEntity.hasEffect(effect()) -> {
				return Optional.of(Objects.requireNonNull(livingEntity.getEffect(effect())).getAmplifier());
			}
			case LivingEntity ignored -> {
				//  No-op; the entity doesn't have the effect
			}
			case null ->
				entityContext.reportProblem("Entity doesn't exist!");
			default ->
				entityContext.reportProblem("Entity is not a living entity!");
		}

		return Optional.empty();

	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}
