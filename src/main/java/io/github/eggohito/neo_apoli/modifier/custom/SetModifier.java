package io.github.eggohito.neo_apoli.modifier.custom;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.modifier.Modifier;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliModifierTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.stream.DoubleStream;

public record SetModifier(List<Modifier> modifiers, FloatProvider amount, Phase phase) implements Modifier {

	public static final MapCodec<SetModifier> CODEC = Modifier.mapCodec(SetModifier::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, SetModifier> STREAM_CODEC = Modifier.streamCodec(SetModifier::new);

	@Override
	public Type<?> getType() {
		return NeoApoliModifierTypes.SET;
	}

	@Override
	public double apply(DoubleStream amounts, double base, double total) {
		return amounts.reduce(total, (left, right) -> right);
	}

}
