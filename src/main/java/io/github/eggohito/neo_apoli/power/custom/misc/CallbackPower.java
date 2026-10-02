package io.github.eggohito.neo_apoli.power.custom.misc;

import com.mojang.datafixers.Products;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.action.custom.NothingAction;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.Power;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.function.BiFunction;

public interface CallbackPower extends Power {

	Action action();

	@Override
	default void validate(ContextValidator validator) {
		Power.super.validate(validator);
		action().validate(validator.forChild(".action"));
	}

	static <P extends CallbackPower> Products.P1<RecordCodecBuilder.Mu<P>, Action> addActionField(RecordCodecBuilder.Instance<P> instance) {
		return instance.group(Action.CODEC.fieldOf("action").forGetter(CallbackPower::action));
	}

	static <P extends CallbackPower> Products.P1<RecordCodecBuilder.Mu<P>, Action> addOptionalActionField(RecordCodecBuilder.Instance<P> instance) {
		return instance.group(Action.CODEC.optionalFieldOf("action", NothingAction.INSTANCE).forGetter(CallbackPower::action));
	}

	static <P extends CallbackPower> MapCodec<P> codec(BiFunction<Optional<Condition>, Action, P> constructor) {
		return RecordCodecBuilder.mapCodec(instance -> Power.addActiveConditionField(instance)
			.and(addActionField(instance).t1())
			.apply(instance, constructor)
		);
	}

	static <P extends CallbackPower> StreamCodec<RegistryFriendlyByteBuf, P> streamCodec(BiFunction<Optional<Condition>, Action, P> constructor) {
		return StreamCodec.composite(
			ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
			Action.STREAM_CODEC, CallbackPower::action,
			constructor
		);
	}

}
