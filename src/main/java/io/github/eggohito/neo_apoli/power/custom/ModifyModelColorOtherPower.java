package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.color.Color;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public record ModifyModelColorOtherPower(Optional<Condition> activeCondition, Color color) implements Power {

	public static final MapCodec<ModifyModelColorOtherPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(Color.CODEC.fieldOf("color").forGetter(ModifyModelColorOtherPower::color))
		.apply(instance, ModifyModelColorOtherPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyModelColorOtherPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		Color.STREAM_CODEC, ModifyModelColorOtherPower::color,
		ModifyModelColorOtherPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MODIFY_MODEL_COLOR_OTHER;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		Power.super.validate(validator);
		color().validate(validator.forChild(".color"));
	}

	public static class Instance extends Power.Instance<ModifyModelColorOtherPower> {

		protected Instance(@NotNull ModifyModelColorOtherPower power) {
			super(power);
		}

		public Context createContext(@NotNull Entity holder, @NotNull Entity rendered) {
			return this.createHolderContextBuilder(holder)
				.withRequired(NeoApoliContextParameters.ACTOR_ENTITY, holder)
				.withRequired(NeoApoliContextParameters.TARGET_ENTITY, rendered)
				.build(holder.level());
		}

		public int color(Context context) {
			return power.color().intValue(context.forChild(".color"));
		}

	}

	public static int modify(@NotNull Entity viewer, @NotNull Entity rendered, int color) {

		for (var instance : Powers.getInstances(viewer, Instance.class)) {

			Context context = instance.createContext(viewer, rendered);

			if (!Objects.equals(viewer, rendered) && instance.isActive(context)) {
				color = Color.mix(color, instance.color(context));
			}

		}

		return color;

	}

}
