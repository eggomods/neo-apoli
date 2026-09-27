package io.github.eggohito.neo_apoli.provider.custom.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParams;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBlockProviderTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextBlockProvider(Context.Parameter<CachedBlock> parameter) implements BlockProvider {

	public static final MapCodec<ContextBlockProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParams.Codecs.BLOCK.fieldOf("parameter").forGetter(ContextBlockProvider::parameter))
		.apply(instance, ContextBlockProvider::new)
	);

	public static final Codec<ContextBlockProvider> INLINE_CODEC = NeoApoliContextParams.Codecs.BLOCK.xmap(
		ContextBlockProvider::new,
		ContextBlockProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextBlockProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParams.StreamCodecs.BLOCK, ContextBlockProvider::parameter,
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
	public Set<ContextKey<?>> getRequiredParameters() {
		return Set.of(parameter());
	}

}
