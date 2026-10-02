package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record VectorComponentFloatProvider(Vec3Provider vector, Direction.Axis axis) implements FloatProvider {

	public static final MapCodec<VectorComponentFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Vec3Provider.CODEC.fieldOf("vector").forGetter(VectorComponentFloatProvider::vector),
		Direction.Axis.CODEC.fieldOf("axis").forGetter(VectorComponentFloatProvider::axis)
	).apply(instance, VectorComponentFloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, VectorComponentFloatProvider> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC, VectorComponentFloatProvider::vector,
		NeoApoliStreamCodecs.AXIS, VectorComponentFloatProvider::axis,
		VectorComponentFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.VECTOR_COMPONENT;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return vector()
			.getValue(context.forChild(".vector"))
			.map(vector -> (float) vector.get(axis()));
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		vector().validate(validator.forChild(".vector"));
	}

}
