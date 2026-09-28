package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.manager.ConditionManager;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextParams;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public record ReferenceCondition(ResourceLocation value, Map<Context.Parameter<?>, ValueProvider<?>> parameters) implements Condition {

	public static final MapCodec<ReferenceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("value").forGetter(ReferenceCondition::value),
		Context.Parameter.VALUE_MAP_CODEC.optionalFieldOf("parameters", Map.of()).forGetter(ReferenceCondition::parameters)
	).apply(instance, ReferenceCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ReferenceCondition> STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, ReferenceCondition::value,
		ByteBufCodecs.fromCodecTrusted(Context.Parameter.VALUE_MAP_CODEC), ReferenceCondition::parameters,
		ReferenceCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.REFERENCE;
	}

	@Override
	public boolean test(Context context) {
		return ConditionManager.getInstance().getAsResult(this.value()).mapOrElse(
			condition -> {

				try {

					if (context.visitor().push(condition)) {

						Context.Builder builder = new Context.Builder(context);
						parameters().forEach((parameter, provider) -> putParameter(context, builder, parameter, provider));

						return condition.test(builder.build(context.level()).forChild(".{\"" + this.value() + "\"}"));

					}

					else {
						context.forChild(".value").reportProblem("Condition with ID \"" + this.value() + "\" was tested recursively!");
					}

				}

				finally {
					context.visitor().pop(condition);
				}

				return false;

			},
			error -> false
		);
	}

	@Override
	public void validate(Context.Validator validator) {

		Condition.super.validate(validator);
		ContextParams conditionParams = Util.make(new ContextParams.Builder(), builder -> parameters().keySet().forEach(builder::required)).build();

		ResourceKey<Condition> valueKey = ResourceKey.create(NeoApoliRegistryKeys.CONDITION, this.value());
		Context.Validator valueValidator = validator.forChild(".value");

		if (validator.hasVisited(valueKey)) {
			valueValidator.reportProblem("Condition with ID \"" + valueKey.location() + "\" was referenced recursively!");
		}

		else {

			Context.Validator parametersValidator = validator.forChild(".parameters");
			parameters().forEach((parameter, provider) -> provider.validate(parametersValidator.forChild(".\"" + parameter.name() + "\"")));

			ConditionManager.getInstance().getAsResult(this.value())
				.ifSuccess(condition -> condition.validate(validator.withParams(conditionParams).visitChild(".{\"" + valueKey.location() + "\"}", valueKey)))
				.ifError(error -> valueValidator.reportProblem(error.message()));

		}

	}

	@SuppressWarnings("unchecked")
	private static void putParameter(Context context, Context.Builder builder, Context.Parameter<?> parameter, ValueProvider<?> provider) {
		builder.withOptional((Context.Parameter<Object>) parameter, (Optional<Object>) provider.getValue(context.forChild(parameter.name() + "\"")));
	}

}
