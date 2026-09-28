package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.color.Color;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public record ModifyModelColorSelfPower(Optional<Condition> activeCondition, Color color) implements Power {

	public static final MapCodec<ModifyModelColorSelfPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(Color.CODEC.fieldOf("color").forGetter(ModifyModelColorSelfPower::color))
		.apply(instance, ModifyModelColorSelfPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyModelColorSelfPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		Color.STREAM_CODEC, ModifyModelColorSelfPower::color,
		ModifyModelColorSelfPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MODIFY_MODEL_COLOR_SELF;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(Context.Validator validator) {
		Power.super.validate(validator);
		color().validate(validator.forChild(".color"));
	}

	public static class Instance extends Power.Instance<ModifyModelColorSelfPower> {

		protected Instance(@NotNull ModifyModelColorSelfPower power) {
			super(power);
		}

		public Context createContext(@NotNull Entity holder, @Nullable Entity viewer) {
			return this.createHolderContextBuilder(holder)
				.withNullable(NeoApoliContextParameters.ACTOR_ENTITY, viewer)
				.withRequired(NeoApoliContextParameters.TARGET_ENTITY, holder)
				.build(holder.level());
		}

		public int getColor(Context context) {
			return power.color().intValue(context.forChild(".color"));
		}

	}

	public static int modify(@Nullable Entity viewer, @NotNull Entity rendered, int color) {

		for (var instance : Powers.getInstances(rendered, Instance.class)) {

			Context context = instance.createContext(rendered, viewer);

			if (viewer == null || Objects.equals(viewer, rendered) || instance.isActive(context)) {
				color = Color.mix(color, instance.getColor(context));
			}

		}

		return color;

	}

}
