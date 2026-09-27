package io.github.eggohito.neo_apoli.provider;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.util.conditional.Conditional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public interface ConditionalValueProvider<Value, Provider extends ValueProvider<Value>> extends ValueProvider<Value>, Conditional<Provider> {

	@Override
	default Optional<Value> getValue(Context context) {
		return this.select(context).flatMap(selected -> selected.provider().getValue(selected.context()));
	}

	default Optional<Selected<Provider>> select(Context context) {

		Context conditionContext = context.forChild(".condition");
		boolean provide = condition().test(conditionContext);

		if (conditionContext.hasProblems()) {
			return Optional.empty();
		}

		else if (provide) {
			return Optional.of(new Selected<>(this.onTrue(), context.forChild(".on_true")));
		}

		else {
			return Optional.of(new Selected<>(this.onFalse(), context.forChild(".on_false")));
		}

	}

	@Override
	default void validate(Context.Validator validator) {

		ValueProvider.super.validate(validator);
		condition().validate(validator.forChild(".condition"));

		onTrue().validate(validator.forChild(".on_true"));
		onFalse().validate(validator.forChild(".on_false"));

	}

	static <P extends ValueProvider<?>, M extends ConditionalValueProvider<?, P>> MapCodec<M> mapCodec(Codec<P> providerCodec, Function3<Condition, P, P, M> constructor) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
			Condition.CODEC.fieldOf("condition").forGetter(ConditionalValueProvider::condition),
			providerCodec.fieldOf("on_true").forGetter(ConditionalValueProvider::onTrue),
			providerCodec.fieldOf("on_false").forGetter(ConditionalValueProvider::onFalse)
		).apply(instance, constructor));
	}

	static <P extends ValueProvider<?>, M extends ConditionalValueProvider<?, P>> StreamCodec<RegistryFriendlyByteBuf, M> streamCodec(StreamCodec<RegistryFriendlyByteBuf, P> providerCodec, Function3<Condition, P, P, M> constructor) {
		return StreamCodec.composite(
			Condition.STREAM_CODEC, ConditionalValueProvider::condition,
			providerCodec, ConditionalValueProvider::onTrue,
			providerCodec, ConditionalValueProvider::onFalse,
			constructor
		);
	}

	record Selected<Provider>(Provider provider, Context context) {

	}

}
