package io.github.eggohito.neo_apoli.color.custom.dynamic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.color.DynamicColor;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliColorTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;

public record DynamicArgb(FloatProvider alpha, FloatProvider red, FloatProvider green, FloatProvider blue) implements DynamicColor {

	public static final MapCodec<DynamicArgb> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("alpha").forGetter(DynamicArgb::alpha),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("red").forGetter(DynamicArgb::red),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("green").forGetter(DynamicArgb::green),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("blue").forGetter(DynamicArgb::blue)
	).apply(instance, DynamicArgb::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, DynamicArgb> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, DynamicArgb::alpha,
		FloatProvider.STREAM_CODEC, DynamicArgb::red,
		FloatProvider.STREAM_CODEC, DynamicArgb::green,
		FloatProvider.STREAM_CODEC, DynamicArgb::blue,
		DynamicArgb::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliColorTypes.DYNAMIC_ARGB;
	}

	@Override
	public int intValue(Context context) {
		return ARGB.colorFromFloat(alpha(context), red(context), green(context), blue(context));
	}

	@Override
	public void validate(ContextValidator validator) {
		DynamicColor.super.validate(validator);
		alpha().validate(validator.forChild(".alpha"));
		red().validate(validator.forChild(".red"));
		green().validate(validator.forChild(".green"));
		blue().validate(validator.forChild(".blue"));
	}

	public float alpha(Context context) {
		return DynamicColor.getValue(context.forChild(".alpha"), alpha()::getFloat, () -> 1.0F);
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

}
