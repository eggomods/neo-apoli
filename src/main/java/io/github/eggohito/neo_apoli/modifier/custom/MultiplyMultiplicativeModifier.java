package io.github.eggohito.neo_apoli.modifier.custom;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.modifier.Modifier;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliModifierTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.stream.DoubleStream;

public record MultiplyMultiplicativeModifier(List<Modifier> modifiers, FloatProvider amount, Phase phase) implements Modifier {

	public static final MapCodec<MultiplyMultiplicativeModifier> CODEC = Modifier.mapCodec(MultiplyMultiplicativeModifier::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, MultiplyMultiplicativeModifier> STREAM_CODEC = Modifier.streamCodec(MultiplyMultiplicativeModifier::new);

	@Override
	public Type<?> getType() {
		return NeoApoliModifierTypes.MULTIPLY_MULTIPLICATIVE;
	}

	@Override
	public double apply(DoubleStream amounts, double base, double total) {
		return total * (1.0 + amounts.reduce(0.0, Double::sum));
	}

}
