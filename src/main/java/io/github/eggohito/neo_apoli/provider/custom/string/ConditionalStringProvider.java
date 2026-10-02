package io.github.eggohito.neo_apoli.provider.custom.string;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.provider.ConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliStringProviderTypes;
import io.github.eggohito.neo_apoli.util.MapCodecUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record ConditionalStringProvider(Condition condition, StringProvider onTrue, StringProvider onFalse) implements StringProvider, ConditionalValueProvider<String, StringProvider> {

	public static final MapCodec<ConditionalStringProvider> CODEC = MapCodecUtil.lazy(ConditionalStringProvider.class.getSimpleName(), () -> ConditionalValueProvider.mapCodec(StringProvider.CODEC, ConditionalStringProvider::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalStringProvider> STREAM_CODEC = StreamCodecUtil.lazy(ConditionalStringProvider.class.getSimpleName(), () -> ConditionalValueProvider.streamCodec(StringProvider.STREAM_CODEC, ConditionalStringProvider::new));

	@Override
	public @NotNull StringProvider.Type<?> getType() {
		return NeoApoliStringProviderTypes.CONDITIONAL;
	}

}
