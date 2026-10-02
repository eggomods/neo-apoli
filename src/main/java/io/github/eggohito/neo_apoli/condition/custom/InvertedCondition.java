package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record InvertedCondition(Condition condition) implements Condition {

	public static final MapCodec<InvertedCondition> CODEC = Condition.CODEC.fieldOf("condition").xmap(
		InvertedCondition::new,
		InvertedCondition::condition
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, InvertedCondition> STREAM_CODEC = Condition.STREAM_CODEC.map(
		InvertedCondition::new,
		InvertedCondition::condition
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.INVERTED;
	}

	@Override
	public boolean test(Context context) {
		return !condition().test(context.forChild(".condition"));
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		condition().validate(validator.forChild(".condition"));
	}

}
