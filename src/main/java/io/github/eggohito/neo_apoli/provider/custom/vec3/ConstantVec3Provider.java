package io.github.eggohito.neo_apoli.provider.custom.vec3;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliVec3ProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ConstantVec3Provider(Vec3 value) implements Vec3Provider {

	public static final MapCodec<ConstantVec3Provider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.DOUBLE.fieldOf("x").forGetter(ConstantVec3Provider::x),
		Codec.DOUBLE.fieldOf("y").forGetter(ConstantVec3Provider::y),
		Codec.DOUBLE.fieldOf("z").forGetter(ConstantVec3Provider::z)
	).apply(instance, ConstantVec3Provider::new));

	public static final Codec<ConstantVec3Provider> INLINE_CODEC = Vec3.CODEC.xmap(
		ConstantVec3Provider::new,
		ConstantVec3Provider::value
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ConstantVec3Provider> STREAM_CODEC = Vec3.STREAM_CODEC.map(
		ConstantVec3Provider::new,
		ConstantVec3Provider::value
	).cast();

	public ConstantVec3Provider(double x, double y, double z) {
		this(new Vec3(x, y, z));
	}

	@Override
	public @NotNull Vec3Provider.Type<?> getType() {
		return NeoApoliVec3ProviderTypes.CONSTANT;
	}

	@Override
	public Optional<Vec3> getValue(Context context) {
		return Optional.of(value());
	}

	public double x() {
		return value().x();
	}

	public double y() {
		return value().y();
	}

	public double z() {
		return value().z();
	}

}
