package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.provider.ConditionalValueProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import io.github.eggohito.neo_apoli.util.MapCodecUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record ConditionalIntProvider(Condition condition, IntProvider onTrue, IntProvider onFalse) implements IntProvider, ConditionalValueProvider<Integer, IntProvider> {

	public static final MapCodec<ConditionalIntProvider> CODEC = MapCodecUtil.lazy(ConditionalIntProvider.class.getSimpleName(), () -> ConditionalValueProvider.mapCodec(IntProvider.CODEC, ConditionalIntProvider::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalIntProvider> STREAM_CODEC = StreamCodecUtil.lazy(ConditionalIntProvider.class.getSimpleName(), () -> ConditionalValueProvider.streamCodec(IntProvider.STREAM_CODEC, ConditionalIntProvider::new));

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.CONDITIONAL;
	}

}
