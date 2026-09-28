package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.color.Color;
import io.github.eggohito.neo_apoli.color.custom.Argb;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.visitor.ClearableVisitor;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ModifyGlowingSelfPower(Optional<Condition> activeCondition, BooleanProvider useTeamColors, Color color) implements Power {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<ModifyGlowingSelfPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(BooleanProvider.CODEC.optionalFieldOf("use_team_color", new ConstantBooleanProvider(true)).forGetter(ModifyGlowingSelfPower::useTeamColors))
		.and(Color.CODEC.optionalFieldOf("color", Argb.DEFAULT).forGetter(ModifyGlowingSelfPower::color))
		.apply(instance, ModifyGlowingSelfPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyGlowingSelfPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		BooleanProvider.STREAM_CODEC, ModifyGlowingSelfPower::useTeamColors,
		Color.STREAM_CODEC, ModifyGlowingSelfPower::color,
		ModifyGlowingSelfPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MODIFY_GLOWING_SELF;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(Context.Validator validator) {

		Power.super.validate(validator);

		useTeamColors().validate(validator.forChild(".use_team_color"));
		color().validate(validator.forChild(".color"));

	}

	public static class Instance extends Power.Instance<ModifyGlowingSelfPower> {

		protected Instance(@NotNull ModifyGlowingSelfPower power) {
			super(power);
		}

		public Context createContext(@NotNull Entity holder, @Nullable Entity viewer) {
			return this.createHolderContextBuilder(holder)
				.withNullable(NeoApoliContextParameters.ACTOR_ENTITY, viewer)
				.withRequired(NeoApoliContextParameters.TARGET_ENTITY, holder)
				.build(holder.level());
		}

		public boolean doesApply(Context context, boolean hasTeamColor) {
			return this.isActive(context)
				&& (!hasTeamColor || !this.shouldUseTeamColor(context));
		}

		public boolean shouldUseTeamColor(Context context) {
			return power.useTeamColors().getBoolean(context.forChild(".use_team_color"));
		}

		public int color(Context context) {
			return power.color().intValue(context.forChild(".color"));
		}

	}

	public static boolean modifyGlowing(Entity viewer, @NotNull Entity rendered) {

		for (var instance : Powers.getInstances(rendered, Instance.class)) {

			Context context = instance.createContext(rendered, viewer);

			try {

				if (VISITOR.push(instance) && instance.isActive(context)) {
					return true;
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return false;

	}

	public static int modifyColor(Entity viewer, @NotNull Entity rendered, boolean hasTeamColor, int color) {

		for (var instance : Powers.getInstances(rendered, Instance.class)) {

			Context context = instance.createContext(rendered, viewer);

			try {

				if (VISITOR.push(instance) && instance.doesApply(context, hasTeamColor)) {
					color = Color.mix(color, instance.color(context));
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return color;

	}

}
