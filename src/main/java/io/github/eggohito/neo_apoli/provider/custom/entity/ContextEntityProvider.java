package io.github.eggohito.neo_apoli.provider.custom.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.EntityContextParameter;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliEntityProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextEntityProvider(EntityContextParameter parameter) implements EntityProvider {

	public static final MapCodec<ContextEntityProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(EntityContextParameter.CODEC.fieldOf("parameter").forGetter(ContextEntityProvider::parameter))
		.apply(instance, ContextEntityProvider::new)
	);

	public static final Codec<ContextEntityProvider> INLINE_CODEC = EntityContextParameter.CODEC.xmap(
		ContextEntityProvider::new,
		ContextEntityProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextEntityProvider> STREAM_CODEC = StreamCodec.composite(
		EntityContextParameter.STREAM_CODEC, ContextEntityProvider::parameter,
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
	public Set<ContextParameter<?>> getRequiredParameters() {
		return Set.of(parameter());
	}

}
