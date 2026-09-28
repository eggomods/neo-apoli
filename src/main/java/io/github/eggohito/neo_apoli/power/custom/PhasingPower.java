package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.custom.IsEntitySneakingCondition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.BlockContextParameter;
import io.github.eggohito.neo_apoli.context.visitor.ClearableVisitor;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.CodecUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiPredicate;

public record PhasingPower(Optional<Condition> activeCondition, Condition phaseDownCondition, RenderEffect renderEffect, float viewDistance) implements Power {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();
	public static final BlockContextParameter PHASED_BLOCK = new BlockContextParameter(NeoApoli.id("phased_block"));

	public static final MapCodec<PhasingPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(Condition.CODEC.optionalFieldOf("phase_down_condition", new IsEntitySneakingCondition(NeoApoliContextParameters.THIS_ENTITY)).forGetter(PhasingPower::phaseDownCondition))
		.and(RenderEffect.CODEC.optionalFieldOf("render_effect", RenderEffect.BLINDNESS).forGetter(PhasingPower::renderEffect))
		.and(Codec.floatRange(2.0F, Float.MAX_VALUE).optionalFieldOf("view_distance", 8.0F).forGetter(PhasingPower::viewDistance))
		.apply(instance, PhasingPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, PhasingPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		Condition.STREAM_CODEC, PhasingPower::phaseDownCondition,
		RenderEffect.STREAM_CODEC, PhasingPower::renderEffect,
		ByteBufCodecs.FLOAT, PhasingPower::viewDistance,
		PhasingPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.PHASING;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(Context.Validator validator) {
		Power.super.validate(validator);
		phaseDownCondition().validate(validator.forChild(".phase_down_condition"));
	}

	public static class Instance extends Power.Instance<PhasingPower> {

		protected Instance(@NotNull PhasingPower power) {
			super(power);
		}

		public Context createContext(Entity holder, CachedBlock cachedBlock) {
			return this.createHolderContextBuilder(holder)
				.withRequired(PHASED_BLOCK, cachedBlock)
				.build(holder.level());
		}

		public RenderEffect renderEffect() {
			return power.renderEffect();
		}

		public boolean doesApply(Entity holder, Context context, BlockPos blockPos, VoxelShape blockShape) {
			return holder.getY() < (double) blockPos.getY() + blockShape.max(Direction.Axis.Y) - (holder.onGround() ? 8.05 / 16.0 : 0.0015)
				|| power.phaseDownCondition().test(context.forChild(".phase_down_condition"));
		}

		public float viewDistance() {
			return power.viewDistance();
		}

	}

	public static boolean doesApply(Entity entity, CachedBlock cachedBlock, VoxelShape blockShape) {
		return doesApply(entity, cachedBlock, (instance, context) -> instance.isActive(context) && instance.doesApply(entity, context, cachedBlock.pos(), blockShape));
	}

	public static boolean doesApply(Entity entity, CachedBlock cachedBlock, BiPredicate<Instance, Context> tester) {

		for (var instance : Powers.getInstances(entity, Instance.class)) {

			Context context = instance.createContext(entity, cachedBlock);

			try {

				if (VISITOR.push(instance) && tester.test(instance, context)) {
					return true;
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return false;

	}

	public static float modifyRenderDistance(Entity entity, CachedBlock cachedBlock, float renderDistance) {

		for (var instance : Powers.getInstances(entity, Instance.class)) {

			Context context = instance.createContext(entity, cachedBlock);

			try {

				if (VISITOR.push(instance) && instance.isActive(context)) {
					renderDistance = Math.min(renderDistance, instance.viewDistance());
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return renderDistance;

	}

	public enum RenderEffect implements StringRepresentable {

		BLINDNESS("blindness"),
		NONE("none");

		public static final Codec<RenderEffect> CODEC = CodecUtil.enumType(RenderEffect.class);
		public static final StreamCodec<ByteBuf, RenderEffect> STREAM_CODEC = StreamCodecUtil.enumType(RenderEffect.class);

		private final String name;

		RenderEffect(String name) {
			this.name = name;
		}

		@Override
		public @NotNull String getSerializedName() {
			return name;
		}

	}

}
