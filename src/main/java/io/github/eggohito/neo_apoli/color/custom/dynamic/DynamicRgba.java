package io.github.eggohito.neo_apoli.color.custom.dynamic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.color.DynamicColor;
import io.github.eggohito.neo_apoli.color.custom.Argb;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliColorTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DynamicRgba(FloatProvider red, FloatProvider green, FloatProvider blue, FloatProvider alpha) implements DynamicColor {

	public static final MapCodec<DynamicRgba> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("red").forGetter(DynamicRgba::red),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("green").forGetter(DynamicRgba::green),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("blue").forGetter(DynamicRgba::blue),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("alpha").forGetter(DynamicRgba::alpha)
	).apply(instance, DynamicRgba::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, DynamicRgba> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, DynamicRgba::red,
		FloatProvider.STREAM_CODEC, DynamicRgba::green,
		FloatProvider.STREAM_CODEC, DynamicRgba::blue,
		FloatProvider.STREAM_CODEC, DynamicRgba::alpha,
		DynamicRgba::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliColorTypes.DYNAMIC_RGBA;
	}

	@Override
	public void validate(ContextValidator validator) {
		DynamicColor.super.validate(validator);
		red().validate(validator.forChild(".red"));
		green().validate(validator.forChild(".green"));
		blue().validate(validator.forChild(".blue"));
		alpha().validate(validator.forChild(".alpha"));
	}

	@Override
	public int intValue(Context context) {
		return new Argb(alpha(context), red(context), green(context), blue(context)).intValue(context);
	}

	public float red(Context context) {
		return DynamicColor.getValue(context.forChild(".red"), red()::getFloat, () -> 1.0F);
	}

	public float green(Context context) {
		return DynamicColor.getValue(context.forChild(".green"), green()::getFloat, () -> 1.0F);
	}

	public float blue(Context context) {
		return DynamicColor.getValue(context.forChild(".blue"), blue()::getFloat, () -> 1.0F);
	}

	public float alpha(Context context) {
		return DynamicColor.getValue(context.forChild(".alpha"), alpha()::getFloat, () -> 1.0F);
	}

}
