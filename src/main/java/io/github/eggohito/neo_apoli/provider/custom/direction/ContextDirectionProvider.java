package io.github.eggohito.neo_apoli.provider.custom.direction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.DirectionContextParameter;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliDirectionProviderTypes;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextDirectionProvider(DirectionContextParameter parameter) implements DirectionProvider {

	public static final MapCodec<ContextDirectionProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParameterTypes.DIRECTION.codec().fieldOf("parameter").forGetter(ContextDirectionProvider::parameter))
		.apply(instance, ContextDirectionProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextDirectionProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParameterTypes.DIRECTION.streamCodec(), ContextDirectionProvider::parameter,
		ContextDirectionProvider::new
	);

	@Override
	public DirectionProvider.@NotNull Type<?> getType() {
		return NeoApoliDirectionProviderTypes.CONTEXT;
	}

	@Override
	public Optional<Direction> getValue(Context context) {

		if (!context.hasParameter(parameter())) {
			context.reportProblem("Parameter \"" + parameter().name() + "\" is not provided in the context!");
		}

		return context.getOptional(parameter());

	}

	@Override
	public Set<ContextParameter<?>> getRequiredParameters() {
		return Set.of(parameter());
	}

}
