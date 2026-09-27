package io.github.eggohito.neo_apoli.provider.custom.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParams;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliItemProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextItemProvider(Context.Parameter<ItemStack> parameter) implements ItemProvider {

	public static final MapCodec<ContextItemProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParams.Codecs.ITEM.fieldOf("parameter").forGetter(ContextItemProvider::parameter))
		.apply(instance, ContextItemProvider::new)
	);

	public static final Codec<ContextItemProvider> INLINE_CODEC = NeoApoliContextParams.Codecs.ITEM.xmap(
		ContextItemProvider::new,
		ContextItemProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextItemProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParams.StreamCodecs.ITEM, ContextItemProvider::parameter,
		ContextItemProvider::new
	);

	@Override
	public ItemProvider.@NotNull Type<?> getType() {
		return NeoApoliItemProviderTypes.CONTEXT;
	}

	@Override
	public Optional<ItemStack> getValue(Context context) {

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
