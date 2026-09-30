package io.github.eggohito.neo_apoli.color.custom.dynamic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.color.DynamicColor;
import io.github.eggohito.neo_apoli.color.custom.Hsv;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliColorTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DynamicHsv(FloatProvider hue, FloatProvider saturation, FloatProvider value, FloatProvider alpha) implements DynamicColor {

	public static final MapCodec<DynamicHsv> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.clamped(0.0F, 360.0F).fieldOf("hue").forGetter(DynamicHsv::hue),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("saturation").forGetter(DynamicHsv::saturation),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("value").forGetter(DynamicHsv::value),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("alpha").forGetter(DynamicHsv::alpha)
	).apply(instance, DynamicHsv::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, DynamicHsv> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, DynamicHsv::hue,
		FloatProvider.STREAM_CODEC, DynamicHsv::saturation,
		FloatProvider.STREAM_CODEC, DynamicHsv::value,
		FloatProvider.STREAM_CODEC, DynamicHsv::alpha,
		DynamicHsv::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliColorTypes.DYNAMIC_HSV;
	}

	@Override
	public int intValue(Context context) {
		return new Hsv(hue(context), saturation(context), this.value(context), alpha(context)).intValue(context);
	}

	@Override
	public void validate(ContextValidator validator) {
		DynamicColor.super.validate(validator);
		hue().validate(validator.forChild(".hue"));
		saturation().validate(validator.forChild(".saturation"));
		value().validate(validator.forChild(".value"));
		alpha().validate(validator.forChild(".alpha"));
	}

	public float hue(Context context) {
		return DynamicColor.getValue(context.forChild(".hue"), hue()::getFloat, () -> 360.0F);
	}

	public float saturation(Context context) {
		return DynamicColor.getValue(context.forChild(".saturation"), saturation()::getFloat, () -> 1.0F);
	}

	public float value(Context context) {
		return DynamicColor.getValue(context.forChild(".value"), value()::getFloat, () -> 1.0F);
	}

	public float alpha(Context context) {
		return DynamicColor.getValue(context.forChild(".alpha"), alpha()::getFloat, () -> 1.0F);
	}

}
