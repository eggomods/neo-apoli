package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record BrightnessFloatProvider(Vec3Provider position) implements FloatProvider {

	public static final MapCodec<BrightnessFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(Vec3Provider.CODEC.fieldOf("position").forGetter(BrightnessFloatProvider::position))
		.apply(instance, BrightnessFloatProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, BrightnessFloatProvider> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC, BrightnessFloatProvider::position,
		BrightnessFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.BRIGHTNESS;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		//noinspection deprecation
		return position().getValue(context.forChild(".position"))
			.map(BlockPos::containing)
			.map(position -> context.level().getLightLevelDependentMagicValue(position));
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		position().validate(validator.forChild(".position"));
	}

}
