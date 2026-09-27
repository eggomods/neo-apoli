package io.github.eggohito.neo_apoli.comparison.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.comparison.Comparator;
import io.github.eggohito.neo_apoli.comparison.Comparison;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.string.StringProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliComparisonTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Locale;

public record StringComparison(Comparator comparator, StringProvider first, StringProvider second, BooleanProvider caseSensitive) implements Comparison {

	public static final MapCodec<StringComparison> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Comparator.CODEC.fieldOf("comparator").forGetter(StringComparison::comparator),
		StringProvider.CODEC.fieldOf("first").forGetter(StringComparison::first),
		StringProvider.CODEC.fieldOf("second").forGetter(StringComparison::second),
		BooleanProvider.CODEC.optionalFieldOf("case_sensitive", new ConstantBooleanProvider(true)).forGetter(StringComparison::caseSensitive)
	).apply(instance, StringComparison::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, StringComparison> STREAM_CODEC = StreamCodec.composite(
		Comparator.STREAM_CODEC, StringComparison::comparator,
		StringProvider.STREAM_CODEC, StringComparison::first,
		StringProvider.STREAM_CODEC, StringComparison::second,
		BooleanProvider.STREAM_CODEC, StringComparison::caseSensitive,
		StringComparison::new
	);

	@Override
	public Type<?> type() {
		return NeoApoliComparisonTypes.STRING;
	}

	@Override
	public boolean compare(Context context) {

		String first = first()
			.getValue(context.forChild(".first"))
			.orElse(null);

		if (first == null) {
			return false;
		}

		String second = second()
			.getValue(context.forChild(".second"))
			.orElse(null);

		if (second == null) {
			return false;
		}

		Context caseSensitiveContext = context.forChild(".case_sensitive");
		boolean caseSensitive = caseSensitive().getBoolean(caseSensitiveContext);

		if (!caseSensitiveContext.hasProblems() && !caseSensitive) {
			first = first.toLowerCase(Locale.ROOT);
			second = second.toLowerCase(Locale.ROOT);
		}

		return comparator().compare(first, second);

	}

	@Override
	public void validate(Context.Validator validator) {

		Comparison.super.validate(validator);

		first().validate(validator.forChild(".first"));
		second().validate(validator.forChild(".second"));
		caseSensitive().validate(validator.forChild(".case_sensitive"));

	}

}
