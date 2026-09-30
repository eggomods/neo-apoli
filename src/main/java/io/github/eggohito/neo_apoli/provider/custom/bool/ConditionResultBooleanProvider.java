package io.github.eggohito.neo_apoli.provider.custom.bool;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBooleanProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ConditionResultBooleanProvider(Condition condition) implements BooleanProvider {

	public static final MapCodec<ConditionResultBooleanProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.fieldOf("condition").forGetter(ConditionResultBooleanProvider::condition)
	).apply(instance, ConditionResultBooleanProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionResultBooleanProvider> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC, ConditionResultBooleanProvider::condition,
		ConditionResultBooleanProvider::new
	);

	@Override
	public @NotNull BooleanProvider.Type<?> getType() {
		return NeoApoliBooleanProviderTypes.CONDITION_RESULT;
	}

	@Override
	public Optional<Boolean> getValue(Context context) {

		Context conditionContext = context.forChild(".condition");
		boolean result = condition().test(conditionContext);

		return conditionContext.hasProblems()
			? Optional.empty()
			: Optional.of(result);

	}

	@Override
	public void validate(ContextValidator validator) {
		BooleanProvider.super.validate(validator);
		condition().validate(validator.forChild(".condition"));
	}

}
