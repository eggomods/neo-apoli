package io.github.eggohito.neo_apoli.modifier.custom;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.modifier.Modifier;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliModifierTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.stream.DoubleStream;

public record AddModifier(List<Modifier> modifiers, FloatProvider amount, Phase phase) implements Modifier {

	public static final MapCodec<AddModifier> CODEC = Modifier.mapCodec(AddModifier::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, AddModifier> STREAM_CODEC = Modifier.streamCodec(AddModifier::new);

	@Override
	public Type<?> getType() {
		return NeoApoliModifierTypes.ADD;
	}

	@Override
	public double apply(DoubleStream amounts, double base, double total) {
		return amounts.reduce(total, Double::sum);
	}

}
