package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.box.BoxProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record BoxSizeFloatProvider(BoxProvider box) implements FloatProvider {

	public static final MapCodec<BoxSizeFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(BoxProvider.CODEC.fieldOf("box").forGetter(BoxSizeFloatProvider::box))
		.apply(instance, BoxSizeFloatProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, BoxSizeFloatProvider> STREAM_CODEC = StreamCodec.composite(
		BoxProvider.STREAM_CODEC, BoxSizeFloatProvider::box,
		BoxSizeFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.BOX_SIZE;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return box().getValue(context.forChild(".box")).map(box -> (float) box.getSize());
	}

	@Override
	public void validate(Context.Validator validator) {
		FloatProvider.super.validate(validator);
		box().validate(validator.forChild(".box"));
	}

}
