package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.custom.IsEntitySneakingCondition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.parameter.BlockContextParameter;
import io.github.eggohito.neo_apoli.context.visitor.ClearableVisitor;
import io.github.eggohito.neo_apoli.exception.PosOutOfBoundsException;
import io.github.eggohito.neo_apoli.exception.PosUnloadedException;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiPredicate;

public record ModifyClimbingPower(Optional<Condition> activeCondition, Condition holdingCondition, BooleanProvider allowHolding) implements Power {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();
	public static final BlockContextParameter CLIMBED_BLOCK = new BlockContextParameter(NeoApoli.id("climbed_block"));

	public static final MapCodec<ModifyClimbingPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(Condition.CODEC.optionalFieldOf("holding_condition", new IsEntitySneakingCondition(NeoApoliContextParameters.THIS_ENTITY)).forGetter(ModifyClimbingPower::holdingCondition))
		.and(BooleanProvider.CODEC.optionalFieldOf("allow_holding", new ConstantBooleanProvider(true)).forGetter(ModifyClimbingPower::allowHolding))
		.apply(instance, ModifyClimbingPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyClimbingPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		Condition.STREAM_CODEC, ModifyClimbingPower::holdingCondition,
		BooleanProvider.STREAM_CODEC, ModifyClimbingPower::allowHolding,
		ModifyClimbingPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MODIFY_CLIMBING;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {

		Power.super.validate(validator);

		holdingCondition().validate(validator.forChild(".holding_condition"));
		allowHolding().validate(validator.forChild(".allow_holding"));

	}

	public static class Instance extends Power.Instance<ModifyClimbingPower> {

		protected Instance(@NotNull ModifyClimbingPower power) {
			super(power);
		}

		@Override
		public Context.Builder createHolderContextBuilder(Entity holder) {

			Level level = holder.level();
			BlockPos blockPos = holder.blockPosition();

			return super.createHolderContextBuilder(holder).withRequired(CLIMBED_BLOCK, CachedBlock.fromLoadedPos(level, blockPos));

		}

		public boolean canHold(Context context) {
			return this.isActive(context)
				&& power.allowHolding().getBoolean(context.forChild(".allow_holding"))
				&& power.holdingCondition().test(context.forChild(".holding_condition"));
		}

	}

	public static boolean modify(Entity entity, BiPredicate<Instance, Context> tester) {

		for (var instance : Powers.getInstances(entity, Instance.class)) {

			try {

				Context context = instance.createHolderContext(entity);

				if (VISITOR.push(instance) && tester.test(instance, context)) {
					return true;
				}

			}

			catch (PosUnloadedException | PosOutOfBoundsException ignored) {
				//  No-op; just need to soft error
			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return false;

	}

}
