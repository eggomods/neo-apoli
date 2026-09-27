package io.github.eggohito.neo_apoli.provider.custom.string;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.CompositeConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliStringProviderTypes;
import io.github.eggohito.neo_apoli.util.MapCodecUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import io.github.eggohito.neo_apoli.util.conditional.CompositeConditional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CompositeConditionalStringProvider(List<CompositeConditional.Entry<StringProvider>> entries, StringProvider defaultValue) implements StringProvider, CompositeConditionalValueProvider<String, StringProvider> {

	public static final MapCodec<CompositeConditionalStringProvider> MAP_CODEC = MapCodecUtil.lazy(CompositeConditionalStringProvider.class.getSimpleName(), () -> CompositeConditionalValueProvider.mapCodec(StringProvider.CODEC, CompositeConditionalStringProvider::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, CompositeConditionalStringProvider> STREAM_CODEC = StreamCodecUtil.lazy(CompositeConditionalStringProvider.class.getSimpleName(), () -> CompositeConditionalValueProvider.streamCodec(StringProvider.STREAM_CODEC, CompositeConditionalStringProvider::new));

	@Override
	public @NotNull StringProvider.Type<?> getType() {
		return NeoApoliStringProviderTypes.COMPOSITE_CONDITIONAL;
	}

}
