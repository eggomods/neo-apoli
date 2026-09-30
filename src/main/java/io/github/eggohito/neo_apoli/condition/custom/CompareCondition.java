package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.comparison.Comparison;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record CompareCondition(Comparison comparison) implements Condition {

	public static final MapCodec<CompareCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(Comparison.CODEC.fieldOf("comparison").forGetter(CompareCondition::comparison))
		.apply(instance, CompareCondition::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, CompareCondition> STREAM_CODEC = StreamCodec.composite(
		Comparison.STREAM_CODEC, CompareCondition::comparison,
		CompareCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.COMPARE;
	}

	@Override
	public boolean test(Context context) {
		return comparison().compare(context);
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		comparison().validate(validator.forChild(".comparison"));
	}

}
