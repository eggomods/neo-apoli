package io.github.eggohito.neo_apoli.provider.custom.damage_source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.DamageSourceContextParameter;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliDamageSourceProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextDamageSourceProvider(DamageSourceContextParameter parameter) implements DamageSourceProvider {

	public static final MapCodec<ContextDamageSourceProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParameterTypes.DAMAGE_SOURCE.codec().fieldOf("parameter").forGetter(ContextDamageSourceProvider::parameter))
		.apply(instance, ContextDamageSourceProvider::new)
	);

	public static final Codec<ContextDamageSourceProvider> INLINE_CODEC = NeoApoliContextParameterTypes.DAMAGE_SOURCE.codec().xmap(
		ContextDamageSourceProvider::new,
		ContextDamageSourceProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextDamageSourceProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParameterTypes.DAMAGE_SOURCE.streamCodec(), ContextDamageSourceProvider::parameter,
		ContextDamageSourceProvider::new
	);

	@Override
	public DamageSourceProvider.@NotNull Type<?> getType() {
		return NeoApoliDamageSourceProviderTypes.CONTEXT;
	}

	@Override
	public Optional<DamageSource> getValue(Context context) {

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
