package io.github.eggohito.neo_apoli.context;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public record ContextParameterMap(Map<ContextParameter<?>, ValueProvider<?>> map) implements ContextValidatable {

	public static final ContextParameterMap EMPTY = new ContextParameterMap(Map.of());

	public static final Codec<ContextParameterMap> CODEC = ContextParameter.VALUE_MAP_CODEC.xmap(ContextParameterMap::new, ContextParameterMap::map);
	public static final StreamCodec<RegistryFriendlyByteBuf, ContextParameterMap> STREAM_CODEC = ContextParameter.VALUE_MAP_STREAM_CODEC.map(ContextParameterMap::new, ContextParameterMap::map);

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
