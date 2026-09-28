package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.action.ActionHolder;
import io.github.eggohito.neo_apoli.action.manager.ActionManager;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextParams;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public record ReferenceAction(ResourceLocation value, Map<Context.Parameter<?>, ValueProvider<?>> parameters) implements Action {

	public static final MapCodec<ReferenceAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("value").forGetter(ReferenceAction::value),
		Context.Parameter.VALUE_MAP_CODEC.optionalFieldOf("parameters", Map.of()).forGetter(ReferenceAction::parameters)
	).apply(instance, ReferenceAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ReferenceAction> STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, ReferenceAction::value,
		ByteBufCodecs.fromCodecTrusted(Context.Parameter.VALUE_MAP_CODEC), ReferenceAction::parameters,
		ReferenceAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.REFERENCE;
	}

	@Override
	public void execute(Context context) {

		if (!ActionManager.getInstance().contains(this.value())) {
			return;
		}

		Action action = ActionManager.getInstance().get(this.value()).value();
		var visitor = context.visitor();

		try {

			if (visitor.push(action)) {

				Context.Builder builder = new Context.Builder(context);
				parameters().forEach((parameter, provider) -> putParameter(context, builder, parameter, provider));

				action.execute(builder.build(context.level()).forChild(".{\"" + this.value() + "\"}"));

			}

			else {
				context.forChild(".value").reportProblem("Action with ID \"" + this.value() + "\" was executed recursively!");
			}

		}

		finally {
			visitor.pop(action);
		}

	}

	@Override
	public void validate(Context.Validator validator) {

		Action.super.validate(validator);
		ContextParams actionParams = Util.make(new ContextParams.Builder(), builder -> parameters().keySet().forEach(builder::required)).build();

		ResourceKey<Action> actionKey = ResourceKey.create(NeoApoliRegistryKeys.ACTION, this.value());
		Context.Validator valueValidator = validator.forChild(".value");

		if (validator.hasVisited(actionKey)) {
			valueValidator.reportProblem("Action with ID \"" + actionKey.location() + "\" was referenced recursively!");
		}

		else {

			Context.Validator parametersValidator = validator.forChild(".parameters");
			parameters().forEach((parameter, provider) -> provider.validate(parametersValidator.forChild(".\"" + parameter.name() + "\"")));

			DataResult<Action> actionResult = ActionManager.getInstance()
				.getAsResult(this.value())
				.map(ActionHolder::value);

			actionResult
				.ifSuccess(action -> action.validate(validator.withParams(actionParams).visitChild(".{\"" + actionKey.location() + "\"}", actionKey)))
				.ifError(error -> valueValidator.reportProblem(error.message()));

		}

	}

	@SuppressWarnings("unchecked")
	private static void putParameter(Context context, Context.Builder builder, Context.Parameter<?> parameter, ValueProvider<?> provider) {
		builder.withOptional((Context.Parameter<Object>) parameter, (Optional<Object>) provider.getValue(context.forChild(parameter.name() + "\"")));
	}

}
