package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.action.custom.NothingAction;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.custom.misc.CallbackPower;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ConstantIntProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record CallbackPowerTickPower(Optional<Condition> activeCondition, Action action, Action risingAction, Action fallingAction, IntProvider interval) implements CallbackPower {

	public static final MapCodec<CallbackPowerTickPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(CallbackPower.addOptionalActionField(instance).t1())
		.and(Action.CODEC.optionalFieldOf("rising_action", NothingAction.INSTANCE).forGetter(CallbackPowerTickPower::risingAction))
		.and(Action.CODEC.optionalFieldOf("falling_action", NothingAction.INSTANCE).forGetter(CallbackPowerTickPower::fallingAction))
		.and(IntProvider.clamped(0, Integer.MAX_VALUE).optionalFieldOf("interval", new ConstantIntProvider(20)).forGetter(CallbackPowerTickPower::interval))
		.apply(instance, CallbackPowerTickPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackPowerTickPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		Action.STREAM_CODEC, CallbackPowerTickPower::action,
		Action.STREAM_CODEC, CallbackPowerTickPower::risingAction,
		Action.STREAM_CODEC, CallbackPowerTickPower::fallingAction,
		IntProvider.STREAM_CODEC, CallbackPowerTickPower::interval,
		CallbackPowerTickPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.CALLBACK_POWER_TICK;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {

		CallbackPower.super.validate(validator);

		risingAction().validate(validator.forChild(".rising_action"));
		fallingAction().validate(validator.forChild(".falling_action"));
		interval().validate(validator.forChild(".interval"));

	}

	public static class Instance extends Power.Instance<CallbackPowerTickPower> {

		private Integer startTicks;
		private Integer endTicks;

		private boolean wasActive;

		protected Instance(@NotNull CallbackPowerTickPower power) {
			super(power);
		}

		@Override
		public void onTick(Entity holder) {

			Context context = createHolderContext(holder);
			int interval = power.interval().getInt(context.forChild(".interval"));

			if (context.hasProblems()) {

				this.startTicks = null;
				this.endTicks = null;

				this.wasActive = false;

			}

			else {

				int ticks = interval > 0
					? holder.tickCount % interval
					: holder.tickCount;

				if (this.isActive(context)) {

					if (interval > 0 && startTicks == null) {
						this.startTicks = ticks;
						this.endTicks = null;
					}

					else if (interval <= 0 || ticks == startTicks) {

						if (!wasActive) {
							power.risingAction().execute(context.forChild(".rising_action"));
							this.wasActive = true;
						}

						else {
							power.action().execute(context.forChild(".action"));
						}

					}

				}

				else if (wasActive) {

					if (interval > 0 && endTicks == null) {
						this.startTicks = null;
						this.endTicks = ticks;
					}

					else if (interval <= 0 || ticks == endTicks) {
						power.fallingAction().execute(context.forChild(".falling_action"));
						this.wasActive = false;
					}

				}

			}

		}

		@Override
		public boolean shouldTick(Entity holder) {
			return true;
		}

	}

}
