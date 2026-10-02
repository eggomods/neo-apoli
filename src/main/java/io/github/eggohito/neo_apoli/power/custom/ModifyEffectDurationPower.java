package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.event.PowerModifyEvents;
import io.github.eggohito.neo_apoli.modifier.Modifier;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record ModifyEffectDurationPower(Optional<Condition> activeCondition, List<Modifier> modifiers) implements Power {

	public static final MapCodec<ModifyEffectDurationPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(ExtraCodecs.nonEmptyList(Modifier.CODEC.listOf()).fieldOf("modifiers").forGetter(ModifyEffectDurationPower::modifiers))
		.apply(instance, ModifyEffectDurationPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyEffectDurationPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		Modifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ModifyEffectDurationPower::modifiers,
		ModifyEffectDurationPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MODIFY_EFFECT_DURATION;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<ModifyEffectDurationPower> {

		protected Instance(@NotNull ModifyEffectDurationPower power) {
			super(power);
		}

		public Context createContext(Entity holder, MobEffectInstance effectInstance, @Nullable Entity source) {
			return this.createHolderContextBuilder(holder)
				.withNullable(NeoApoliContextParameters.ACTOR_ENTITY, source)
				.withRequired(NeoApoliContextParameters.TARGET_ENTITY, holder)
				.withRequired(NeoApoliContextParameters.APPLIED_EFFECT, effectInstance)
				.build(holder.level());
		}

		public List<Modifier.Operation> operations(Context context) {

			List<Modifier.Operation> result = new ObjectArrayList<>();
			MiscUtil.iterateList(power.modifiers(), (index, modifier) -> result.add(Modifier.operation(modifier, context.forChild(".modifiers[" + index + "]"))));

			return result;

		}

	}

	public static int modify(Entity holder, MobEffectInstance effectInstance, @Nullable Entity source, int duration) {

		List<Modifier.Operation> operations = new ObjectArrayList<>();

		for (var instance : Powers.getInstances(holder, Instance.class)) {

			Context context = instance.createContext(holder, effectInstance, source);

			if (instance.isActive(context)) {
				operations.addAll(instance.operations(context));
			}

		}

		PowerModifyEvents.NUMBER.invoker().beforeModified(NeoApoliPowerTypes.MODIFY_EFFECT_DURATION, operations, duration);
		return (int) Math.round(Modifier.applyAll(operations, duration));

	}

}
