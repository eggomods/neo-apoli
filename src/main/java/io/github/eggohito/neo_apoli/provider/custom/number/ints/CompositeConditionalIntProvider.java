package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.CompositeConditionalValueProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import io.github.eggohito.neo_apoli.util.MapCodecUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CompositeConditionalIntProvider(List<Entry<IntProvider>> entries, IntProvider defaultValue) implements IntProvider, CompositeConditionalValueProvider<Integer, IntProvider> {

	public static final MapCodec<CompositeConditionalIntProvider> CODEC = MapCodecUtil.lazy(CompositeConditionalIntProvider.class.getSimpleName(), () -> CompositeConditionalValueProvider.mapCodec(IntProvider.CODEC, CompositeConditionalIntProvider::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, CompositeConditionalIntProvider> STREAM_CODEC = StreamCodecUtil.lazy(CompositeConditionalIntProvider.class.getSimpleName(), () -> CompositeConditionalValueProvider.streamCodec(IntProvider.STREAM_CODEC, CompositeConditionalIntProvider::new));

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.COMPOSITE_CONDITIONAL;
	}

}
