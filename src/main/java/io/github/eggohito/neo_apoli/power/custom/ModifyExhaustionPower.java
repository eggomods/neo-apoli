package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.event.PowerModifyEvents;
import io.github.eggohito.neo_apoli.modifier.Modifier;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record ModifyExhaustionPower(Optional<Condition> activeCondition, List<Modifier> modifiers) implements Power {

	public static final MapCodec<ModifyExhaustionPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(ExtraCodecs.nonEmptyList(Modifier.CODEC.listOf()).fieldOf("modifiers").forGetter(ModifyExhaustionPower::modifiers))
		.apply(instance, ModifyExhaustionPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyExhaustionPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		Modifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ModifyExhaustionPower::modifiers,
		ModifyExhaustionPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MODIFY_EXHAUSTION;
	}

	@Override
	public Instance createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		Power.super.validate(validator);
		ContextValidatable.validate(modifiers(), validator, index -> ".modifiers[" + index + "]");
	}

	public static class Instance extends Power.Instance<ModifyExhaustionPower> {

		protected Instance(@NotNull ModifyExhaustionPower power) {
			super(power);
		}

		public List<Modifier> modifiers() {
			return power.modifiers();
		}

	}

	public static float modify(Player player, float exhaustion) {

		List<Modifier.Operation> operations = new ObjectArrayList<>();

		for (var instance : Powers.getInstances(player, Instance.class)) {

			Context context = instance.createHolderContext(player);

			if (instance.isActive(context)) {
				MiscUtil.iterateList(instance.modifiers(), (index, modifier) -> operations.add(Modifier.operation(modifier, context.forChild(".modifiers[" + index + "]"))));
			}

		}

		PowerModifyEvents.NUMBER.invoker().beforeModified(NeoApoliPowerTypes.MODIFY_EXHAUSTION, operations, exhaustion);
		return (float) Modifier.applyAll(operations, exhaustion);

	}

}
