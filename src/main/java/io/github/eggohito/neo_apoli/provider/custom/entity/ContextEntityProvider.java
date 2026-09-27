package io.github.eggohito.neo_apoli.provider.custom.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParams;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliEntityProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextEntityProvider(Context.Parameter<Entity> parameter) implements EntityProvider {

	public static final MapCodec<ContextEntityProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParams.Codecs.ENTITY.fieldOf("parameter").forGetter(ContextEntityProvider::parameter))
		.apply(instance, ContextEntityProvider::new)
	);

	public static final Codec<ContextEntityProvider> INLINE_CODEC = NeoApoliContextParams.Codecs.ENTITY.xmap(
		ContextEntityProvider::new,
		ContextEntityProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextEntityProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParams.StreamCodecs.ENTITY, ContextEntityProvider::parameter,
		ContextEntityProvider::new
	);

	@Override
	public EntityProvider.@NotNull Type<?> getType() {
		return NeoApoliEntityProviderTypes.CONTEXT;
	}

	@Override
	public Optional<Entity> getValue(Context context) {

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
