package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.MultiIntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record MinIntProvider(List<IntProvider> values) implements MultiIntProvider {

	public static final MapCodec<MinIntProvider> CODEC = MultiIntProvider.mapCodec(MinIntProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, MinIntProvider> STREAM_CODEC = MultiIntProvider.streamCodec(MinIntProvider::new);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.MIN;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		return this.iterateAndProcess(context, Math::min);
	}

}
