package io.github.eggohito.neo_apoli.provider.custom.direction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliDirectionProviderTypes;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ConstantDirectionProvider(Direction value) implements DirectionProvider {

	public static final MapCodec<ConstantDirectionProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliCodecs.DIRECTION.fieldOf("value").forGetter(ConstantDirectionProvider::value))
		.apply(instance, ConstantDirectionProvider::new)
	);

	public static final Codec<ConstantDirectionProvider> INLINE_CODEC = NeoApoliCodecs.DIRECTION.xmap(
		ConstantDirectionProvider::new,
		ConstantDirectionProvider::value
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ConstantDirectionProvider> STREAM_CODEC = StreamCodec.composite(
		Direction.STREAM_CODEC, ConstantDirectionProvider::value,
		ConstantDirectionProvider::new
	);

	@Override
	public DirectionProvider.@NotNull Type<?> getType() {
		return NeoApoliDirectionProviderTypes.CONSTANT;
	}

	@Override
	public Optional<Direction> getValue(Context context) {
		return Optional.of(value());
	}

}
