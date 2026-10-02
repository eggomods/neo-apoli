package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ConstantIntProvider;
import io.github.eggohito.neo_apoli.provider.custom.slot.SlotProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.SlotAccess;

public record ConsumeItemAction(IntProvider amount, SlotProvider slot) implements Action {

	public static final MapCodec<ConsumeItemAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		IntProvider.CODEC.optionalFieldOf("amount", new ConstantIntProvider(1)).forGetter(ConsumeItemAction::amount),
		SlotProvider.CODEC.fieldOf("slot").forGetter(ConsumeItemAction::slot)
	).apply(instance, ConsumeItemAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ConsumeItemAction> STREAM_CODEC = StreamCodec.composite(
		IntProvider.STREAM_CODEC, ConsumeItemAction::amount,
		SlotProvider.STREAM_CODEC, ConsumeItemAction::slot,
		ConsumeItemAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.CONSUME_ITEM;
	}

	@Override
	public void execute(Context context) {

		if (context.level().isClientSide()) {
			return;
		}

		SlotAccess slot = slot()
			.getValue(context.forChild(".slot"))
			.orElse(null);

		if (slot == null) {
			return;
		}

		Context amountContext = context.forChild(".amount");
		int amount = Math.abs(amount().getInt(amountContext));

		if (!amountContext.hasProblems()) {
			slot.get().shrink(amount);
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		amount().validate(validator.forChild(".amount"));
		slot().validate(validator.forChild(".slot"));
	}

}
