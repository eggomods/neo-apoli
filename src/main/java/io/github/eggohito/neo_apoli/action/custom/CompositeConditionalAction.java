package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import io.github.eggohito.neo_apoli.util.conditional.CompositeConditional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.List;

public record CompositeConditionalAction(List<Entry<Action>> entries, Action defaultValue) implements Action, CompositeConditional<Action> {

	public static final MapCodec<CompositeConditionalAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		CompositeConditional.Entry.codec(Condition.CODEC.fieldOf("condition"), Action.CODEC.fieldOf("action")).listOf().fieldOf("entries").forGetter(CompositeConditionalAction::entries),
		Action.CODEC.optionalFieldOf("default", NothingAction.INSTANCE).forGetter(CompositeConditionalAction::defaultValue)
	).apply(instance, CompositeConditionalAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CompositeConditionalAction> STREAM_CODEC = StreamCodec.composite(
		CompositeConditional.Entry.streamCodec(Condition.STREAM_CODEC, Action.STREAM_CODEC).apply(ByteBufCodecs.list()), CompositeConditionalAction::entries,
		Action.STREAM_CODEC, CompositeConditionalAction::defaultValue,
		CompositeConditionalAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.COMPOSITE_CONDITIONAL;
	}

	@Override
	public void execute(Context context) {

		MutableBoolean continueCondition = new MutableBoolean(true);
		MiscUtil.iterateList(
			entries(),
			(index, entry) -> {

				Context entryContext = context.forChild(".entries[" + index + "]");
				boolean result = entry.condition().test(entryContext.forChild(".condition"));

				if (result) {
					entry.value().execute(entryContext.forChild(".action"));
				}

				continueCondition.setValue(!result);

			},
			continueCondition::isTrue
		);

		if (continueCondition.isTrue()) {
			defaultValue().execute(context.forChild(".default"));
		}

	}

	@Override
	public void validate(ContextValidator validator) {

		Action.super.validate(validator);
		MiscUtil.iterateList(
			entries(),
			(index, entry) -> {

				ContextValidator entryValidator = validator.forChild(".entries[" + index + "]");

				entry.condition().validate(entryValidator.forChild(".condition"));
				entry.value().validate(entryValidator.forChild(".action"));

			}
		);

		defaultValue().validate(validator.forChild(".default"));

	}

}
