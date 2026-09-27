package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParams;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextFloatProvider(Context.Parameter<Float> parameter) implements FloatProvider {

	public static final MapCodec<ContextFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParams.Codecs.FLOAT.fieldOf("parameter").forGetter(ContextFloatProvider::parameter))
		.apply(instance, ContextFloatProvider::new)
	);

	public static final Codec<ContextFloatProvider> INLINE_CODEC = NeoApoliContextParams.Codecs.FLOAT.xmap(
		ContextFloatProvider::new,
		ContextFloatProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextFloatProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParams.StreamCodecs.FLOAT, ContextFloatProvider::parameter,
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
	public Set<ContextKey<?>> getRequiredParameters() {
		return Set.of(parameter());
	}

}
