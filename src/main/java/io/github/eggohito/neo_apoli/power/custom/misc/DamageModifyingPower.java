package io.github.eggohito.neo_apoli.power.custom.misc;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.action.custom.NothingAction;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.visitor.ClearableVisitor;
import io.github.eggohito.neo_apoli.event.PowerModifyEvents;
import io.github.eggohito.neo_apoli.modifier.Modifier;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface DamageModifyingPower extends Power {

	ClearableVisitor<Instance<?>> VISITOR = ClearableVisitor.createThreadLocalized();

	List<Modifier> modifiers();

	Action onModifyAction();

	@Override
	default void validate(ContextValidator validator) {

		Power.super.validate(validator);

		ContextValidatable.validate(this.modifiers(), validator, index -> ".modifiers[" + index + "]");
		onModifyAction().validate(validator.forChild(".on_modify_action"));

	}

	static <P extends DamageModifyingPower> MapCodec<P> codec(Function3<Optional<Condition>, List<Modifier>, Action, P> constructor) {
		return RecordCodecBuilder.mapCodec(instance -> Power.addActiveConditionField(instance)
			.and(Modifier.CODEC.listOf().fieldOf("modifiers").forGetter(DamageModifyingPower::modifiers))
			.and(Action.CODEC.optionalFieldOf("on_modify_action", NothingAction.INSTANCE).forGetter(DamageModifyingPower::onModifyAction))
			.apply(instance, constructor)
		);
	}

	static <P extends DamageModifyingPower> StreamCodec<RegistryFriendlyByteBuf, P> streamCodec(Function3<Optional<Condition>, List<Modifier>, Action, P> constructor) {
		return StreamCodec.composite(
			ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
			ByteBufCodecs.collection(ObjectArrayList::new, Modifier.STREAM_CODEC), DamageModifyingPower::modifiers,
			Action.STREAM_CODEC, DamageModifyingPower::onModifyAction,
			constructor
		);
	}

	abstract class Instance<P extends DamageModifyingPower> extends Power.Instance<P> {

		public Instance(@NotNull P power) {
			super(power);
		}

		public abstract Context createDamageContext(Entity actor, Entity target, DamageSource source, float amount);

		public List<Modifier> modifiers() {
			return power.modifiers();
		}

		public List<Modifier.Operation> operations(Context context) {

			List<Modifier.Operation> result = new ObjectArrayList<>();
			MiscUtil.iterateList(power.modifiers(), (index, modifier) -> result.add(Modifier.operation(modifier, context.forChild(".modifiers[" + index + "]"))));

			return result;

		}

		public void execute(Context context) {
			power.onModifyAction().execute(context.forChild(".on_modify_action"));
		}

	}

	static <P extends DamageModifyingPower, I extends DamageModifyingPower.Instance<P>> float modify(Type<P> type, Class<I> instanceClass, Entity holder, Entity actor, Entity target, DamageSource source, float amount) {

		List<Modifier.Operation> operations = new ObjectArrayList<>();

		for (var instance : Powers.getInstances(holder, instanceClass)) {

			Context context = instance.createDamageContext(actor, target, source, amount);

			try {

				if (VISITOR.push(instance) && instance.isActive(context)) {
					operations.addAll(instance.operations(context));
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		PowerModifyEvents.NUMBER.invoker().beforeModified(type, operations, amount);
		return (float) Modifier.applyAll(operations, amount);

	}

}
