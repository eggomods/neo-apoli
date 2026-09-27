package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import io.github.eggohito.neo_apoli.util.MapCodecUtil;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record WeightedIntProvider(WeightedList<IntProvider> entries) implements IntProvider {

	public static final MapCodec<WeightedIntProvider> CODEC = MapCodecUtil.lazy(WeightedIntProvider.class.getSimpleName(), () -> RecordCodecBuilder.mapCodec(instance -> instance
		.group(WeightedList.codec(IntProvider.CODEC).fieldOf("entries").forGetter(WeightedIntProvider::entries))
		.apply(instance, WeightedIntProvider::new)
	));

	public static final StreamCodec<RegistryFriendlyByteBuf, WeightedIntProvider> STREAM_CODEC = StreamCodecUtil.lazy(WeightedIntProvider.class.getSimpleName(), () -> StreamCodec.composite(
		StreamCodecUtil.weightedList(IntProvider.STREAM_CODEC), WeightedIntProvider::entries,
		WeightedIntProvider::new
	));

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.WEIGHTED;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		return entries()
			.neo_apoli$getRandomAndIndex(context.level().getRandom())
			.flatMap(pair -> pair.first().getValue(context.forChild(".entries[" + pair.secondInt() + "]")));
	}

	@Override
	public void validate(Context.Validator validator) {
		IntProvider.super.validate(validator);
		MiscUtil.iterateList(entries().unwrap(), (index, entry) -> entry.value().validate(validator.forChild(".entries[" + index + "]")));
	}

}
