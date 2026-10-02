package io.github.eggohito.neo_apoli.provider.custom.string;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.StringContextParameter;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliStringProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextStringProvider(StringContextParameter parameter) implements StringProvider {

	public static final MapCodec<ContextStringProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParameterTypes.STRING.codec().fieldOf("parameter").forGetter(ContextStringProvider::parameter))
		.apply(instance, ContextStringProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextStringProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParameterTypes.STRING.streamCodec(), ContextStringProvider::parameter,
		ContextStringProvider::new
	);

	@Override
	public @NotNull StringProvider.Type<?> getType() {
		return NeoApoliStringProviderTypes.CONTEXT;
	}

	@Override
	public Optional<String> getValue(Context context) {

		if (!context.hasParameter(parameter())) {
			context.reportProblem("Parameter \"" + parameter.name() + "\" is not provided in the context!");
		}

		return context.getOptional(parameter());

	}

	@Override
	public Set<ContextParameter<?>> getRequiredParameters() {
		return Set.of(parameter());
	}

}
