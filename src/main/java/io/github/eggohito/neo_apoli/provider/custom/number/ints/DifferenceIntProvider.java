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

public record DifferenceIntProvider(List<IntProvider> values) implements MultiIntProvider {

	public static final MapCodec<DifferenceIntProvider> CODEC = MultiIntProvider.mapCodec(DifferenceIntProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, DifferenceIntProvider> STREAM_CODEC = MultiIntProvider.streamCodec(DifferenceIntProvider::new);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.DIFFERENCE;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		return this.iterateAndProcess(context, (first, second) -> first - second);
	}

}
