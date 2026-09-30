package io.github.eggohito.neo_apoli.provider.custom.string;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliStringProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record FromFloatStringProvider(FloatProvider value) implements StringProvider {

	public static final MapCodec<FromFloatStringProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(FloatProvider.CODEC.fieldOf("value").forGetter(FromFloatStringProvider::value))
		.apply(instance, FromFloatStringProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, FromFloatStringProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, FromFloatStringProvider::value,
		FromFloatStringProvider::new
	);

	@Override
	public @NotNull StringProvider.Type<?> getType() {
		return NeoApoliStringProviderTypes.FROM_FLOAT;
	}

	@Override
	public Optional<String> getValue(Context context) {

		Context valueContext = context.forChild(".value");
		float value = value().getFloat(valueContext);

		return valueContext.hasProblems()
			? Optional.empty()
			: Optional.of(Float.toString(value));

	}

	@Override
	public void validate(ContextValidator validator) {
		StringProvider.super.validate(validator);
		value().validate(validator.forChild(".value"));
	}

}
