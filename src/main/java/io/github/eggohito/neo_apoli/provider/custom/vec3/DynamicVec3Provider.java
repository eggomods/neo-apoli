package io.github.eggohito.neo_apoli.provider.custom.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliVec3ProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record DynamicVec3Provider(FloatProvider x, FloatProvider y, FloatProvider z) implements Vec3Provider {

	public static final MapCodec<DynamicVec3Provider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.CODEC.fieldOf("x").forGetter(DynamicVec3Provider::x),
		FloatProvider.CODEC.fieldOf("y").forGetter(DynamicVec3Provider::y),
		FloatProvider.CODEC.fieldOf("z").forGetter(DynamicVec3Provider::z)
	).apply(instance, DynamicVec3Provider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, DynamicVec3Provider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, DynamicVec3Provider::x,
		FloatProvider.STREAM_CODEC, DynamicVec3Provider::y,
		FloatProvider.STREAM_CODEC, DynamicVec3Provider::z,
		DynamicVec3Provider::new
	);

	@Override
	public @NotNull Vec3Provider.Type<?> getType() {
		return NeoApoliVec3ProviderTypes.DYNAMIC;
	}

	@Override
	public Optional<Vec3> getValue(Context context) {
		return Optional.of(new Vec3(
			x().getFloat(context.forChild(".x")),
			y().getFloat(context.forChild(".y")),
			z().getFloat(context.forChild(".z"))
		));
	}

	@Override
	public void validate(ContextValidator validator) {

		Vec3Provider.super.validate(validator);

		x().validate(validator.forChild(".x"));
		y().validate(validator.forChild(".y"));
		z().validate(validator.forChild(".z"));

	}

}
