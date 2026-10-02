package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record FromFloatIntProvider(FloatProvider value) implements IntProvider {

	public static final MapCodec<FromFloatIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(FloatProvider.CODEC.fieldOf("value").forGetter(FromFloatIntProvider::value))
		.apply(instance, FromFloatIntProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, FromFloatIntProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, FromFloatIntProvider::value,
		FromFloatIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.FROM_FLOAT;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		return value().getValue(context.forChild(".value")).map(Float::intValue);
	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		value().validate(validator.forChild(".value"));
	}

}
