package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.MultiFloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record ProductFloatProvider(List<FloatProvider> values) implements MultiFloatProvider {

	public static final MapCodec<ProductFloatProvider> CODEC = MultiFloatProvider.mapCodec(ProductFloatProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ProductFloatProvider> STREAM_CODEC = MultiFloatProvider.streamCodec(ProductFloatProvider::new);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.PRODUCT;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return this.iterateAndProcess(context, (first, second) -> first * second);
	}

}
