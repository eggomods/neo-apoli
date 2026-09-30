package io.github.eggohito.neo_apoli.context;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public record ContextParameters(Map<ContextParameter<?>, ValueProvider<?>> map) implements ContextValidatable {

	public static final ContextParameters EMPTY = new ContextParameters(Map.of());

	public static final Codec<ContextParameters> CODEC = ContextParameter.VALUE_MAP_CODEC.xmap(ContextParameters::new, ContextParameters::map);
	public static final StreamCodec<RegistryFriendlyByteBuf, ContextParameters> STREAM_CODEC = ContextParameter.VALUE_MAP_STREAM_CODEC.map(ContextParameters::new, ContextParameters::map);

	@Override
	public void validate(ContextValidator validator) {
		map().forEach((parameter, provider) -> provider.validate(validator.forChild(".\"" + parameter.name() + "\"")));
	}

	public ContextValidator.Parameters forValidation() {

		var builder = new ContextValidator.Parameters.Builder();
		map().keySet().forEach(builder::required);

		return builder.build();

	}

	@SuppressWarnings("unchecked")
	public Context forUser(Context baseContext, UnaryOperator<Context> parametersResolver) {

		var builder = new Context.Builder(baseContext);
		var resolved = parametersResolver.apply(baseContext);

		map().forEach((parameter, provider) -> builder.withOptional((ContextParameter<Object>) parameter, (Optional<Object>) provider.getValue(resolved.forChild(".\"" + parameter.name() + "\""))));
		return builder.build(baseContext.level());

	}

}
