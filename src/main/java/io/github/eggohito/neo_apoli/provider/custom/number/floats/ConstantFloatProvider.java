package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ConstantFloatProvider(float value) implements FloatProvider {

	public static final MapCodec<ConstantFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(Codec.FLOAT.fieldOf("value").forGetter(ConstantFloatProvider::value))
		.apply(instance, ConstantFloatProvider::new)
	);

	public static final Codec<ConstantFloatProvider> INLINE_CODEC = Codec.FLOAT.xmap(
		ConstantFloatProvider::new,
		ConstantFloatProvider::value
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ConstantFloatProvider> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, ConstantFloatProvider::value,
		ConstantFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.CONSTANT;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return Optional.of(value());
	}

}
