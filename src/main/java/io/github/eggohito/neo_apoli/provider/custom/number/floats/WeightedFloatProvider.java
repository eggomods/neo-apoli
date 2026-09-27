package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import io.github.eggohito.neo_apoli.util.MapCodecUtil;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record WeightedFloatProvider(WeightedList<FloatProvider> entries) implements FloatProvider {

	public static final MapCodec<WeightedFloatProvider> CODEC = MapCodecUtil.lazy(WeightedFloatProvider.class.getSimpleName(), () -> RecordCodecBuilder.mapCodec(instance -> instance
		.group(WeightedList.codec(FloatProvider.CODEC).fieldOf("entries").forGetter(WeightedFloatProvider::entries))
		.apply(instance, WeightedFloatProvider::new)
	));

	public static final StreamCodec<RegistryFriendlyByteBuf, WeightedFloatProvider> STREAM_CODEC = StreamCodecUtil.lazy(WeightedFloatProvider.class.getSimpleName(), () -> StreamCodec.composite(
		StreamCodecUtil.weightedList(FloatProvider.STREAM_CODEC), WeightedFloatProvider::entries,
		WeightedFloatProvider::new
	));

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.WEIGHTED;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return entries()
			.neo_apoli$getRandomAndIndex(context.level().getRandom())
			.flatMap(pair -> pair.first().getValue(context.forChild(".entries[" + pair.secondInt() + "]")));
	}

	@Override
	public void validate(Context.Validator validator) {
		FloatProvider.super.validate(validator);
		MiscUtil.iterateList(entries().unwrap(), (index, entry) -> entry.value().validate(validator.forChild(".entries[" + index + "]")));
	}

}
