package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record ExhaustAction(FloatProvider amount, EntityProvider entity) implements Action {

	public static final MapCodec<ExhaustAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.CODEC.fieldOf("amount").forGetter(ExhaustAction::amount),
		EntityProvider.CODEC.fieldOf("entity").forGetter(ExhaustAction::entity)
	).apply(instance, ExhaustAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ExhaustAction> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, ExhaustAction::amount,
		EntityProvider.STREAM_CODEC, ExhaustAction::entity,
		ExhaustAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.EXHAUST;
	}

	@Override
	public void execute(Context context) {

		if (!(entity().getValue(context.forChild(".entity")).orElse(null) instanceof ServerPlayer serverPlayer)) {
			return;
		}

		Context amountContext = context.forChild(".amount");
		float amount = amount().getFloat(amountContext);

		if (!amountContext.hasProblems() && Math.signum(amount) >= 1.0) {
			serverPlayer.causeFoodExhaustion(amount);
		}

	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		amount().validate(validator.forChild(".amount"));
		entity().validate(validator.forChild(".entity"));
	}

}
