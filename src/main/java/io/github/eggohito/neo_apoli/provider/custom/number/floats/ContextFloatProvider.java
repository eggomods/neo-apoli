package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.FloatContextParameter;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextFloatProvider(FloatContextParameter parameter) implements FloatProvider {

	public static final MapCodec<ContextFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(FloatContextParameter.CODEC.fieldOf("parameter").forGetter(ContextFloatProvider::parameter))
		.apply(instance, ContextFloatProvider::new)
	);

	public static final Codec<ContextFloatProvider> INLINE_CODEC = FloatContextParameter.CODEC.xmap(
		ContextFloatProvider::new,
		ContextFloatProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatContextParameter.STREAM_CODEC, ContextFloatProvider::parameter,
		ContextFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.CONTEXT;
	}

	@Override
	public Optional<Float> getValue(Context context) {

		if (!context.hasParameter(parameter())) {
			context.reportProblem("Parameter \"" + parameter().name() + "\" is not provided in the context!");
		}

		return context.getOptional(parameter());

	}

	@Override
	public Set<Context.Parameter<?>> getRequiredParameters() {
		return Set.of(parameter());
	}

}
