package io.github.eggohito.neo_apoli.provider.custom.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParams;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliEffectProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextEffectProvider(Context.Parameter<MobEffectInstance> parameter) implements EffectProvider {

	public static final MapCodec<ContextEffectProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParams.Codecs.EFFECT.fieldOf("parameter").forGetter(ContextEffectProvider::parameter))
		.apply(instance, ContextEffectProvider::new)
	);

	public static final Codec<ContextEffectProvider> INLINE_CODEC = NeoApoliContextParams.Codecs.EFFECT.xmap(
		ContextEffectProvider::new,
		ContextEffectProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextEffectProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParams.StreamCodecs.EFFECT, ContextEffectProvider::parameter,
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
	public Set<ContextKey<?>> getRequiredParameters() {
		return Set.of(parameter());
	}

}
