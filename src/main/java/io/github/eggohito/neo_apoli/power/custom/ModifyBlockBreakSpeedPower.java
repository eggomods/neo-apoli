package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.visitor.ClearableVisitor;
import io.github.eggohito.neo_apoli.event.PowerModifyEvents;
import io.github.eggohito.neo_apoli.modifier.Modifier;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record ModifyBlockBreakSpeedPower(Optional<Condition> activeCondition, List<Modifier> modifiers) implements Power {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<ModifyBlockBreakSpeedPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(ExtraCodecs.nonEmptyList(Modifier.CODEC.listOf()).fieldOf("modifiers").forGetter(ModifyBlockBreakSpeedPower::modifiers))
		.apply(instance, ModifyBlockBreakSpeedPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyBlockBreakSpeedPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		Modifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ModifyBlockBreakSpeedPower::modifiers,
		ModifyBlockBreakSpeedPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MODIFY_BLOCK_BREAK_SPEED;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<ModifyBlockBreakSpeedPower> {

		protected Instance(@NotNull ModifyBlockBreakSpeedPower power) {
			super(power);
		}

		public Context createContext(Entity holder, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
			return this.createHolderContextBuilder(holder)
					.withRequired(NeoApoliContextParameters.MINING_BLOCK, new CachedBlock(blockPos, blockState, blockEntity))
				.build(holder.level());
		}

		public List<Modifier.Operation> modifiers(Context context) {

			List<Modifier.Operation> result = new ObjectArrayList<>();
			MiscUtil.iterateList(power.modifiers(), (index, modifier) -> result.add(Modifier.operation(modifier, context.forChild(".modifiers[" + index + "]"))));

			return result;

		}

	}

	public static float modify(Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, float breakSpeed) {

		List<Modifier.Operation> entries = new ObjectArrayList<>();

		for (var instance : Powers.getInstances(player, Instance.class)) {

			try {

				Context context = instance.createContext(player, blockPos, blockState, blockEntity);

				if (VISITOR.push(instance) && instance.isActive(context)) {
					entries.addAll(instance.modifiers(context));
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		PowerModifyEvents.NUMBER.invoker().beforeModified(NeoApoliPowerTypes.MODIFY_JUMP, entries, breakSpeed);
		return (float) Modifier.applyAll(entries, breakSpeed);

	}

}
