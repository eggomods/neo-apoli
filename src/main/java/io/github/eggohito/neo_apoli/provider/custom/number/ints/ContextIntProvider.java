package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParams;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextIntProvider(Context.Parameter<Integer> parameter) implements IntProvider {

	public static final MapCodec<ContextIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParams.Codecs.INT.fieldOf("parameter").forGetter(ContextIntProvider::parameter))
		.apply(instance, ContextIntProvider::new)
	);

	public static final Codec<ContextIntProvider> INLINE_CODEC = NeoApoliContextParams.Codecs.INT.xmap(
		ContextIntProvider::new,
		ContextIntProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextIntProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParams.StreamCodecs.INT, ContextIntProvider::parameter,
		ContextIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.CONTEXT;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		if (!context.hasParameter(parameter())) {
			context.reportProblem("Parameter \"" + parameter() + "\" is not provided in the context!");
		}

		return context.getOptional(parameter());

	}

	@Override
	public Set<ContextKey<?>> getRequiredParameters() {
		return Set.of(parameter());
	}

}
