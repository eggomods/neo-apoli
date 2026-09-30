package io.github.eggohito.neo_apoli.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import io.github.eggohito.neo_apoli.util.conditional.CompositeConditional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public interface CompositeConditionalValueProvider<Value, Provider extends ValueProvider<Value>> extends ValueProvider<Value>, CompositeConditional<Provider> {

	@Override
	default Optional<Value> getValue(Context context) {
		var selected = this.select(context);
		return selected.provider().getValue(selected.context());
	}

	default Selected<Provider> select(Context context) {

		var entryIterator = entries().listIterator();

		while (entryIterator.hasNext()) {

			Context entryContext = context.forChild(".entries[" + entryIterator.nextIndex() + "]");
			var entry = entryIterator.next();

			Context conditionContext = entryContext.forChild(".condition");
			boolean provides = entry.condition().test(conditionContext);

			if (!conditionContext.hasProblems() && provides) {
				return new Selected<>(entry.value(), context.forChild(".value"));
			}

		}

		return new Selected<>(defaultValue(), context.forChild(".default"));

	}

	@Override
	default void validate(ContextValidator validator) {

		ValueProvider.super.validate(validator);

		MiscUtil.iterateList(
			entries(),
			(index, entry) -> {

				ContextValidator entryValidator = validator.forChild(".entries[" + index + "]");

				entry.condition().validate(entryValidator.forChild(".condition"));
				entry.value().validate(entryValidator.forChild(".value"));

			}
		);

		defaultValue().validate(validator.forChild(".default"));

	}

	static <P extends ValueProvider<?>, M extends CompositeConditionalValueProvider<?, P>> MapCodec<M> mapCodec(Codec<P> providerCodec, BiFunction<List<CompositeConditional.Entry<P>>, P, M> constructor) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.nonEmptyList(CompositeConditional.Entry.codec(Condition.CODEC, providerCodec).listOf()).fieldOf("entries").forGetter(CompositeConditionalValueProvider::entries),
			providerCodec.fieldOf("default").forGetter(CompositeConditionalValueProvider::defaultValue)
		).apply(instance, constructor));
	}

	static <P extends ValueProvider<?>, M extends CompositeConditionalValueProvider<?, P>> StreamCodec<RegistryFriendlyByteBuf, M> streamCodec(StreamCodec<RegistryFriendlyByteBuf, P> providerCodec, BiFunction<List<CompositeConditional.Entry<P>>, P, M> constructor) {
		return StreamCodec.composite(
			CompositeConditional.Entry.streamCodec(Condition.STREAM_CODEC, providerCodec).apply(ByteBufCodecs.list()), CompositeConditionalValueProvider::entries,
			providerCodec, CompositeConditionalValueProvider::defaultValue,
			constructor
		);
	}

	record Selected<Provider>(Provider provider, Context context) {

	}

}
