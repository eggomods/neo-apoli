package io.github.eggohito.neo_apoli.provider.custom.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.BlockContextParameter;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBlockProviderTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextBlockProvider(BlockContextParameter parameter) implements BlockProvider {

	public static final MapCodec<ContextBlockProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(BlockContextParameter.CODEC.fieldOf("parameter").forGetter(ContextBlockProvider::parameter))
		.apply(instance, ContextBlockProvider::new)
	);

	public static final Codec<ContextBlockProvider> INLINE_CODEC = BlockContextParameter.CODEC.xmap(
		ContextBlockProvider::new,
		ContextBlockProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextBlockProvider> STREAM_CODEC = StreamCodec.composite(
		BlockContextParameter.STREAM_CODEC, ContextBlockProvider::parameter,
		ContextBlockProvider::new
	);

	@Override
	public BlockProvider.@NotNull Type<?> getType() {
		return NeoApoliBlockProviderTypes.CONTEXT;
	}

	@Override
	public Optional<CachedBlock> getValue(Context context) {

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
