package io.github.eggohito.neo_apoli.provider.custom.string;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliStringProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record FromIntStringProvider(IntProvider value) implements StringProvider {

	public static final MapCodec<FromIntStringProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(IntProvider.CODEC.fieldOf("value").forGetter(FromIntStringProvider::value))
		.apply(instance, FromIntStringProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, FromIntStringProvider> STREAM_CODEC = StreamCodec.composite(
		IntProvider.STREAM_CODEC, FromIntStringProvider::value,
		FromIntStringProvider::new
	);

	@Override
	public @NotNull StringProvider.Type<?> getType() {
		return NeoApoliStringProviderTypes.FROM_INT;
	}

	@Override
	public Optional<String> getValue(Context context) {

		Context valueContext = context.forChild(".value");
		int value = value().getInt(valueContext);

		return valueContext.hasProblems()
			? Optional.empty()
			: Optional.of(Integer.toString(value));

	}

	@Override
	public void validate(Context.Validator validator) {
		StringProvider.super.validate(validator);
		value().validate(validator.forChild(".value"));
	}

}
