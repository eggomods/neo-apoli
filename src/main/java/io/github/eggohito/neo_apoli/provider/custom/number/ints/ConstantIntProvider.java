package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ConstantIntProvider(int value) implements IntProvider {

	public static final MapCodec<ConstantIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(Codec.INT.fieldOf("value").forGetter(ConstantIntProvider::value))
		.apply(instance, ConstantIntProvider::new)
	);

	public static final Codec<ConstantIntProvider> INLINE_CODEC = Codec.INT.xmap(
		ConstantIntProvider::new,
		ConstantIntProvider::value
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ConstantIntProvider> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, ConstantIntProvider::value,
		ConstantIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.CONSTANT;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		return Optional.of(value());
	}

}
