package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record FromIntFloatProvider(IntProvider value) implements FloatProvider {

	public static final MapCodec<FromIntFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(IntProvider.CODEC.fieldOf("value").forGetter(FromIntFloatProvider::value))
		.apply(instance, FromIntFloatProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, FromIntFloatProvider> STREAM_CODEC = StreamCodec.composite(
		IntProvider.STREAM_CODEC, FromIntFloatProvider::value,
		FromIntFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.FROM_INT;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return value().getValue(context.forChild(".value")).map(Float::valueOf);
	}

	@Override
	public void validate(Context.Validator validator) {
		FloatProvider.super.validate(validator);
		value().validate(validator.forChild(".value"));
	}

}
