package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.custom.ConstantCondition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.parameter.EffectContextParameter;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record EntityActiveEffectsIntProvider(Condition condition, EntityProvider entity) implements IntProvider {

	public static final EffectContextParameter ACTIVE_EFFECT = new EffectContextParameter(NeoApoli.id("active_effect"));
	public static final ContextValidator.Parameters CONDITION_PARAMETER_SET = new ContextValidator.Parameters.Builder().required(ACTIVE_EFFECT).build();

	public static final MapCodec<EntityActiveEffectsIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.optionalFieldOf("condition", new ConstantCondition(true)).forGetter(EntityActiveEffectsIntProvider::condition),
		EntityProvider.CODEC.fieldOf("entity").forGetter(EntityActiveEffectsIntProvider::entity)
	).apply(instance, EntityActiveEffectsIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityActiveEffectsIntProvider> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC, EntityActiveEffectsIntProvider::condition,
		EntityProvider.STREAM_CODEC, EntityActiveEffectsIntProvider::entity,
		EntityActiveEffectsIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.ENTITY_ACTIVE_EFFECTS;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		if (!(entity().getValue(context.forChild(".entity")).orElse(null) instanceof LivingEntity livingEntity)) {
			return Optional.empty();
		}

		var activeEffects = livingEntity.getActiveEffects();
		int matches = 0;

		for (var activeEffect : activeEffects) {

			Context effectContext = new Context.Builder(context)
				.withRequired(ACTIVE_EFFECT, activeEffect)
				.build(context.level());

			if (condition().test(effectContext.forChild(".condition"))) {
				matches++;
			}

		}

		return Optional.of(matches);

	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		condition().validate(validator.withParams(CONDITION_PARAMETER_SET).forChild(".condition"));
		entity().validate(validator.forChild(".entity"));
	}

}
