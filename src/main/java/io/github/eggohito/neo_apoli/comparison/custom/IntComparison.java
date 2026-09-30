package io.github.eggohito.neo_apoli.comparison.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.comparison.Comparator;
import io.github.eggohito.neo_apoli.comparison.Comparison;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliComparisonTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record IntComparison(Comparator comparator, IntProvider first, IntProvider second) implements Comparison {

	public static final MapCodec<IntComparison> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Comparator.CODEC.fieldOf("comparator").forGetter(IntComparison::comparator),
		IntProvider.CODEC.fieldOf("first").forGetter(IntComparison::first),
		IntProvider.CODEC.fieldOf("second").forGetter(IntComparison::second)
	).apply(instance, IntComparison::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IntComparison> STREAM_CODEC = StreamCodec.composite(
		Comparator.STREAM_CODEC, IntComparison::comparator,
		IntProvider.STREAM_CODEC, IntComparison::first,
		IntProvider.STREAM_CODEC, IntComparison::second,
		IntComparison::new
	);

	@Override
	public Type<?> type() {
		return NeoApoliComparisonTypes.INT;
	}

	@Override
	public boolean compare(Context context) {
		return comparator().compare(
			first().getInt(context.forChild(".first")),
			second().getInt(context.forChild(".second"))
		);
	}

	@Override
	public void validate(ContextValidator validator) {
		Comparison.super.validate(validator);
		first().validate(validator.forChild(".first"));
		second().validate(validator.forChild(".second"));
	}

}
