package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.custom.ConstantCondition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.BlockContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.EntityContextParameter;
import io.github.eggohito.neo_apoli.exception.PosOutOfBoundsException;
import io.github.eggohito.neo_apoli.exception.PosUnloadedException;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParams;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.CodecUtil;
import io.github.eggohito.neo_apoli.util.Shape;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record AreaOfEffectAction(AreaTarget areaTarget, Action areaAction, Condition areaCondition, Vec3Provider position, Shape shape, FloatProvider radius) implements Action {

	public static final Context.Parameter<Entity> ENTITY_IN_AREA = NeoApoliContextParams.registerInternal("entity_in_area", EntityContextParameter::new);
	public static final Context.Parameter<CachedBlock> BLOCK_IN_AREA = NeoApoliContextParams.registerInternal("block_in_area", BlockContextParameter::new);

	public static final ContextKeySet AREA_PARAMETER_SET = new ContextKeySet.Builder()
		.optional(ENTITY_IN_AREA)
		.optional(BLOCK_IN_AREA)
		.build();

	public static final MapCodec<AreaOfEffectAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		AreaTarget.CODEC.fieldOf("area_target").forGetter(AreaOfEffectAction::areaTarget),
		Action.CODEC.fieldOf("area_action").forGetter(AreaOfEffectAction::areaAction),
		Condition.CODEC.optionalFieldOf("area_condition", new ConstantCondition(true)).forGetter(AreaOfEffectAction::areaCondition),
		Vec3Provider.CODEC.fieldOf("position").forGetter(AreaOfEffectAction::position),
		Shape.CODEC.optionalFieldOf("shape", Shape.CUBE).forGetter(AreaOfEffectAction::shape),
		FloatProvider.CODEC.fieldOf("radius").forGetter(AreaOfEffectAction::radius)
	).apply(instance, AreaOfEffectAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, AreaOfEffectAction> STREAM_CODEC = StreamCodec.composite(
		AreaTarget.STREAM_CODEC, AreaOfEffectAction::areaTarget,
		Action.STREAM_CODEC, AreaOfEffectAction::areaAction,
		Condition.STREAM_CODEC, AreaOfEffectAction::areaCondition,
		Vec3Provider.STREAM_CODEC, AreaOfEffectAction::position,
		Shape.STREAM_CODEC, AreaOfEffectAction::shape,
		FloatProvider.STREAM_CODEC, AreaOfEffectAction::radius,
		AreaOfEffectAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.AREA_OF_EFFECT;
	}

	@Override
	public void execute(Context context) {
		position()
			.getValue(context.forChild(".position"))
			.ifPresent(position -> areaTarget().run(context, shape(), areaAction(), areaCondition(), position, radius().getFloat(context.forChild(".radius"))));
	}

	@Override
	public void validate(Context.Validator validator) {

		Action.super.validate(validator);
		var areaValidator = validator.withAdditionalKeysFromSets(AREA_PARAMETER_SET);

		areaAction().validate(areaValidator.forChild(".area_action"));
		areaCondition().validate(areaValidator.forChild(".area_condition"));
		position().validate(validator.forChild(".position"));
		radius().validate(validator.forChild(".radius"));

	}

	//  TODO: Maybe expose and convert this enum as an interface?
	public enum AreaTarget {

		ENTITY {

			@Override
			public void run(Context context, Shape shape, Action areaAction, Condition areaCondition, Vec3 origin, float radius) {

				for (var entity : shape.getEntities(context.level(), origin, radius)) {

					Context areaContext = new Context.Builder(context)
						.withRequired(ENTITY_IN_AREA, entity)
						.build(context.level());

					if (areaCondition.test(areaContext.forChild(".area_condition"))) {
						areaAction.execute(areaContext.forChild(".area_action"));
					}

				}

			}

		},

		BLOCK {

			@Override
			public void run(Context context, Shape shape, Action areaAction, Condition areaCondition, Vec3 origin, float radius) {

				for (var blockPos : shape.getBlockPositions(BlockPos.containing(origin), Math.round(radius))) {

					try {

						Context areaContext = new Context.Builder(context)
							.withRequired(BLOCK_IN_AREA, CachedBlock.fromLoadedPos(context.level(), blockPos))
							.build(context.level());

						if (areaCondition.test(areaContext.forChild(".area_condition"))) {
							areaAction.execute(areaContext.forChild(".area_action"));
						}

					}

					catch (PosUnloadedException | PosOutOfBoundsException ignored) {
						//  No-op
					}

				}

			}

		};

		public static final Codec<AreaTarget> CODEC = CodecUtil.enumType(AreaTarget.class);
		public static final StreamCodec<ByteBuf, AreaTarget> STREAM_CODEC = StreamCodecUtil.enumType(AreaTarget.class);

		public abstract void run(Context context, Shape shape, Action areaAction, Condition areaCondition, Vec3 origin, float radius);

	}

}
