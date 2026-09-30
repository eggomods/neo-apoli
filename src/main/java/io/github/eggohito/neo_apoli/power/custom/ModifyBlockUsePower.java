package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.action.custom.NothingAction;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextUser;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.parameter.BlockContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.DirectionContextParameter;
import io.github.eggohito.neo_apoli.exception.PosOutOfBoundsException;
import io.github.eggohito.neo_apoli.exception.PosUnloadedException;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.custom.misc.PrioritizedPower;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import io.github.eggohito.neo_apoli.util.BlockUsePhase;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import io.github.eggohito.neo_apoli.util.PriorityPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record ModifyBlockUsePower(Optional<Condition> activeCondition, Actions actions, Conditions conditions, EnumSet<BlockUsePhase> usePhases, int priority) implements PrioritizedPower<ModifyBlockUsePower> {

	public static final BlockContextParameter USED_BLOCK = new BlockContextParameter(NeoApoli.id("used_block"));
	public static final DirectionContextParameter USED_SIDE = new DirectionContextParameter(NeoApoli.id("used_side"));

	public static final MapCodec<ModifyBlockUsePower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(Actions.CODEC.forGetter(ModifyBlockUsePower::actions))
		.and(Conditions.CODEC.forGetter(ModifyBlockUsePower::conditions))
		.and(BlockUsePhase.SET_CODEC.optionalFieldOf("use_phases", EnumSet.allOf(BlockUsePhase.class)).forGetter(ModifyBlockUsePower::usePhases))
		.and(Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyBlockUsePower::priority))
		.apply(instance, ModifyBlockUsePower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyBlockUsePower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		Actions.STREAM_CODEC, ModifyBlockUsePower::actions,
		Conditions.STREAM_CODEC, ModifyBlockUsePower::conditions,
		BlockUsePhase.SET_STREAM_CODEC, ModifyBlockUsePower::usePhases,
		ByteBufCodecs.INT, ModifyBlockUsePower::priority,
		ModifyBlockUsePower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MODIFY_BLOCK_USE;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		PrioritizedPower.super.validate(validator);
		actions().validate(validator);
		conditions().validate(validator);
	}

	public static class Instance extends Power.Instance<ModifyBlockUsePower> {

		protected Instance(@NotNull ModifyBlockUsePower power) {
			super(power);
		}

		public Context createContext(Entity holder, BlockHitResult blockResult, InteractionHand hand) {

			Level level = holder.level();
			BlockPos blockPos  = blockResult.getBlockPos();
			SlotAccess usedItemSlot = holder instanceof LivingEntity livingEntity
				? SlotAccess.of(() -> livingEntity.getItemInHand(hand), stack -> livingEntity.setItemInHand(hand, stack))
				: SlotAccess.NULL;

			return this.createHolderContextBuilder(holder)
				.withRequired(USED_BLOCK, CachedBlock.fromLoadedPos(level, blockPos))
				.withRequired(USED_SIDE, blockResult.getDirection())
				.withRequired(NeoApoliContextParameters.USED_ITEM_SLOT, usedItemSlot)
				.withRequired(NeoApoliContextParameters.USED_ITEM, usedItemSlot.get())
				.build(level);

		}

		public InteractionResult apply(Context context) {
			return power.actions().execute(context);
		}

		public boolean doesApply(BlockUsePhase interactionPhase, PriorityPhase priorityPhase) {
			return power.usePhases().contains(interactionPhase)
				&& power.inPhase(priorityPhase);
		}

	}

	public record Actions(Action action, InteractionResult result) implements ContextUser {

		public static final MapCodec<Actions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Action.CODEC.optionalFieldOf("action", NothingAction.INSTANCE).forGetter(Actions::action),
			NeoApoliCodecs.INTERACTION_RESULT.optionalFieldOf("result", InteractionResult.SUCCESS).forGetter(Actions::result)
		).apply(instance, Actions::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Actions> STREAM_CODEC = StreamCodec.composite(
			Action.STREAM_CODEC, Actions::action,
			NeoApoliStreamCodecs.INTERACTION_RESULT, Actions::result,
			Actions::new
		);

		@Override
		public void validate(ContextValidator validator) {
			ContextUser.super.validate(validator);
			action().validate(validator.forChild(".action"));
		}

		public InteractionResult execute(Context context) {
			action().execute(context.forChild(".action"));
			return result();
		}

	}

	public record Conditions(EnumSet<Direction> directions, EnumSet<InteractionHand> hands) implements ContextUser {

		public static final MapCodec<Conditions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			NeoApoliCodecs.DIRECTION_SET.optionalFieldOf("directions", EnumSet.allOf(Direction.class)).forGetter(Conditions::directions),
			NeoApoliCodecs.HAND_SET.optionalFieldOf("hands", EnumSet.allOf(InteractionHand.class)).forGetter(Conditions::hands)
		).apply(instance, Conditions::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Conditions> STREAM_CODEC = StreamCodec.composite(
			NeoApoliStreamCodecs.DIRECTION_SET, Conditions::directions,
			NeoApoliStreamCodecs.HAND_SET, Conditions::hands,
			Conditions::new
		);

		public boolean test(Context context, InteractionHand hand) {
			return context.getOptional(USED_SIDE).map(directions()::contains).orElse(false)
				&& hands().contains(hand);
		}

	}

	public static InteractionResult execute(Player player, InteractionHand hand, BlockHitResult blockHitResult, BlockUsePhase interactionPhase, PriorityPhase priorityPhase, Consumer<InteractionResult> zeroPriorityResultSetter, Supplier<InteractionResult> zeroPriorityResultGetter, Supplier<InteractionResult> defaultValueSupplier) {
		return switch (priorityPhase) {
			case BEFORE ->
				executeOnBeforeBlockUse(player, hand, blockHitResult, interactionPhase, zeroPriorityResultSetter, defaultValueSupplier);
			case AFTER ->
				executeOnAfterBlockUse(player, hand, blockHitResult, interactionPhase, zeroPriorityResultGetter, defaultValueSupplier);
		};
	}

	private static InteractionResult executeOnBeforeBlockUse(Player player, InteractionHand hand, BlockHitResult blockHitResult, BlockUsePhase interactionPhase, Consumer<InteractionResult> zeroPriorityResultSetter, Supplier<InteractionResult> defaultResultSupplier) {

		InstanceCollection<Instance> instanceCollection = new InstanceCollection<>(player, Instance.class, instance -> instance.doesApply(interactionPhase, PriorityPhase.BEFORE));

		for (int priority = instanceCollection.getMaxPriority(); priority >= instanceCollection.getMinPriority(); priority--) {

			if (!instanceCollection.hasInstances(priority)) {
				continue;
			}

			List<Instance> instances = instanceCollection.getInstances(priority);
			InteractionResult previousResult = InteractionResult.PASS;

			for (var instance : instances) {

				try {

					Context context = instance.createContext(player, blockHitResult, hand);

					if (instance.isActive(context)) {
						previousResult = MiscUtil.overrideResult(previousResult, instance.apply(context));
					}

				}

				catch (PosUnloadedException | PosOutOfBoundsException ignored) {
					//  No-op; just need to soft error
				}

			}

			boolean previousPassed = previousResult == InteractionResult.PASS;
			boolean zeroPriority = priority == 0;

			if (previousPassed || zeroPriority) {

				if (zeroPriority) {
					zeroPriorityResultSetter.accept(previousResult);
				}

				continue;

			}

			if (previousResult instanceof InteractionResult.Success(InteractionResult.SwingSource swingSource, InteractionResult.ItemContext ignored) && swingSource != InteractionResult.SwingSource.NONE) {
				player.swing(hand, swingSource == InteractionResult.SwingSource.SERVER);
			}

			return previousResult;

		}

		return defaultResultSupplier.get();

	}

	private static InteractionResult executeOnAfterBlockUse(Player player, InteractionHand hand, BlockHitResult blockHitResult, BlockUsePhase interactionPhase, Supplier<InteractionResult> zeroPriorityResultGetter, Supplier<InteractionResult> defaultResultSupplier) {

		InteractionResult original = defaultResultSupplier.get();
		InteractionResult modified = InteractionResult.PASS;

		InteractionResult zeroPriorityResult = zeroPriorityResultGetter.get();

		if (zeroPriorityResult != null && zeroPriorityResult != InteractionResult.PASS) {
			modified = zeroPriorityResult;
		}

		else if (original == InteractionResult.PASS) {

			InstanceCollection<Instance> instanceCollection = new InstanceCollection<>(player, Instance.class, instance -> instance.doesApply(interactionPhase, PriorityPhase.AFTER));

			for (int priority = instanceCollection.getMaxPriority(); priority >= instanceCollection.getMinPriority(); priority--) {

				if (!instanceCollection.hasInstances(priority)) {
					continue;
				}

				List<Instance> instances = instanceCollection.getInstances(priority);
				InteractionResult previousResult = InteractionResult.PASS;

				for (var instance : instances) {

					try {

						Context context = instance.createContext(player, blockHitResult, hand);

						if (instance.isActive(context)) {
							previousResult = MiscUtil.overrideResult(previousResult, instance.apply(context));
						}

					}

					catch (PosUnloadedException | PosOutOfBoundsException ignored) {
						//  No-op; just need to soft error
					}

				}

				if (previousResult != InteractionResult.PASS) {
					modified = previousResult;
					break;
				}

			}

		}

		if (modified instanceof InteractionResult.Success(InteractionResult.SwingSource swingSource, InteractionResult.ItemContext ignored) && swingSource != InteractionResult.SwingSource.NONE) {
			player.swing(hand, swingSource == InteractionResult.SwingSource.SERVER);
		}

		return MiscUtil.overrideResult(original, modified);

	}

}
