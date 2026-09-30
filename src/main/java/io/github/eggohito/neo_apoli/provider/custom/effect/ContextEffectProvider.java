package io.github.eggohito.neo_apoli.provider.custom.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.EffectContextParameter;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliEffectProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextEffectProvider(EffectContextParameter parameter) implements EffectProvider {

	public static final MapCodec<ContextEffectProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(EffectContextParameter.CODEC.fieldOf("parameter").forGetter(ContextEffectProvider::parameter))
		.apply(instance, ContextEffectProvider::new)
	);

	public static final Codec<ContextEffectProvider> INLINE_CODEC = EffectContextParameter.CODEC.xmap(
		ContextEffectProvider::new,
		ContextEffectProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextEffectProvider> STREAM_CODEC = StreamCodec.composite(
		EffectContextParameter.STREAM_CODEC, ContextEffectProvider::parameter,
		ContextEffectProvider::new
	);

	@Override
	public EffectProvider.@NotNull Type<?> getType() {
		return NeoApoliEffectProviderTypes.CONTEXT;
	}

	@Override
	public Optional<MobEffectInstance> getValue(Context context) {

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
