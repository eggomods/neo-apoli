package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.custom.ConstantCondition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParams;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record EntityActiveEffectsIntProvider(Condition condition, EntityProvider entity) implements IntProvider {

	public static final Context.Parameter<MobEffectInstance> ACTIVE_EFFECT = NeoApoliContextParams.registerSimpleInternal("active_effect", MobEffectInstance.class);
	public static final ContextKeySet CONDITION_PARAMETER_SET = new ContextKeySet.Builder().required(ACTIVE_EFFECT).build();

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
	public void validate(Context.Validator validator) {
		IntProvider.super.validate(validator);
		condition().validate(validator.withAdditionalKeysFromSets(CONDITION_PARAMETER_SET).forChild(".condition"));
		entity().validate(validator.forChild(".entity"));
	}

}
