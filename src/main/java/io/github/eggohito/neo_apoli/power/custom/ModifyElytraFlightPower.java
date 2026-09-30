package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.visitor.ClearableVisitor;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.custom.misc.PrioritizedPower;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public record ModifyElytraFlightPower(Optional<Condition> activeCondition, BooleanProvider allow, int priority) implements PrioritizedPower<ModifyElytraFlightPower> {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<ModifyElytraFlightPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(BooleanProvider.CODEC.fieldOf("allow").forGetter(ModifyElytraFlightPower::allow))
		.and(Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyElytraFlightPower::priority))
		.apply(instance, ModifyElytraFlightPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyElytraFlightPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		BooleanProvider.STREAM_CODEC, ModifyElytraFlightPower::allow,
		ByteBufCodecs.INT, ModifyElytraFlightPower::priority,
		ModifyElytraFlightPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MODIFY_ELYTRA_FLIGHT;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		PrioritizedPower.super.validate(validator);
		allow().validate(validator.forChild(".allow"));
	}

	public static class Instance extends Power.Instance<ModifyElytraFlightPower> {

		protected Instance(@NotNull ModifyElytraFlightPower power) {
			super(power);
		}

		public boolean isAllowed(Context context) {
			return power.allow().getBoolean(context.forChild(".allow"));
		}

	}

	public static boolean modify(Entity entity, BooleanSupplier defaultValue) {

		for (var instance : new InstanceCollection<>(entity, Instance.class)) {

			Context context = instance.createHolderContext(entity);

			try {

				if (VISITOR.push(instance) && instance.isActive(context)) {
					return instance.isAllowed(context);
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return defaultValue.getAsBoolean();

	}

}
