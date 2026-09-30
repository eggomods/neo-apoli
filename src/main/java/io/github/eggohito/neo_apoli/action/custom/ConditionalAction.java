package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.conditional.Conditional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ConditionalAction(Condition condition, Action onTrue, Action onFalse) implements Action, Conditional<Action> {

	public static final MapCodec<ConditionalAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.fieldOf("condition").forGetter(ConditionalAction::condition),
		Action.CODEC.fieldOf("on_true").forGetter(ConditionalAction::onTrue),
		Action.CODEC.optionalFieldOf("on_false", NothingAction.INSTANCE).forGetter(ConditionalAction::onFalse)
	).apply(instance, ConditionalAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalAction> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC, ConditionalAction::condition,
		Action.STREAM_CODEC, ConditionalAction::onTrue,
		Action.STREAM_CODEC, ConditionalAction::onFalse,
		ConditionalAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.CONDITIONAL;
	}

	@Override
	public void execute(Context context) {

		if (condition().test(context.forChild(".condition"))) {
			onTrue().execute(context.forChild(".on_true"));
		}

		else {
			onFalse().execute(context.forChild(".on_false"));
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		condition().validate(validator.forChild(".condition"));
		onTrue().validate(validator.forChild(".on_true"));
		onFalse().validate(validator.forChild(".on_false"));
	}

}
