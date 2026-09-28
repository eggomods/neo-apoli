package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.custom.ConstantCondition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextParams;
import io.github.eggohito.neo_apoli.context.parameter.EntityContextParameter;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.floats.ConstantFloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ConstantIntProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.ConstantVec3Provider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.registry.NeoApoliParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public record SpawnParticlesAction(ParticleOptions particle, Condition viewCondition, Vec3Provider position, Vec3Provider spread, FloatProvider speed, IntProvider count, BooleanProvider force) implements Action {

	public static final EntityContextParameter VIEWER_ENTITY = new EntityContextParameter(NeoApoli.id("viewer_entity"));
	public static final ContextParams CONDITION_PARAMETER_SET = new ContextParams.Builder().required(VIEWER_ENTITY).build();

	public static final MapCodec<SpawnParticlesAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NeoApoliCodecs.PARTICLE_OPTIONS.fieldOf("particle").forGetter(SpawnParticlesAction::particle),
		Condition.CODEC.optionalFieldOf("view_condition", new ConstantCondition(true)).forGetter(SpawnParticlesAction::viewCondition),
		Vec3Provider.CODEC.fieldOf("position").forGetter(SpawnParticlesAction::position),
		Vec3Provider.CODEC.optionalFieldOf("spread", new ConstantVec3Provider(0.5, 0.5, 0.5)).forGetter(SpawnParticlesAction::spread),
		FloatProvider.CODEC.optionalFieldOf("speed", new ConstantFloatProvider(0.0F)).forGetter(SpawnParticlesAction::speed),
		IntProvider.clamped(0, Integer.MAX_VALUE).optionalFieldOf("count", new ConstantIntProvider(1)).forGetter(SpawnParticlesAction::count),
		BooleanProvider.CODEC.optionalFieldOf("force", new ConstantBooleanProvider(false)).forGetter(SpawnParticlesAction::force)
	).apply(instance, SpawnParticlesAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SpawnParticlesAction> STREAM_CODEC = StreamCodec.composite(
		NeoApoliParticleTypes.OPTIONS_STREAM_CODEC, SpawnParticlesAction::particle,
		Condition.STREAM_CODEC, SpawnParticlesAction::viewCondition,
		Vec3Provider.STREAM_CODEC, SpawnParticlesAction::position,
		Vec3Provider.STREAM_CODEC, SpawnParticlesAction::spread,
		FloatProvider.STREAM_CODEC, SpawnParticlesAction::speed,
		IntProvider.STREAM_CODEC, SpawnParticlesAction::count,
		BooleanProvider.STREAM_CODEC, SpawnParticlesAction::force,
		SpawnParticlesAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.SPAWN_PARTICLES;
	}

	@Override
	public void execute(Context context) {

		if (!(context.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		Vec3 position = position()
			.getValue(context.forChild(".position"))
			.orElse(null);

		if (position == null) {
			return;
		}

		Vec3 spread = spread()
			.getValue(context.forChild(".spread"))
			.orElse(null);

		if (spread == null) {
			return;
		}

		boolean force = force().getBoolean(context.forChild(".force"));
		float speed = speed().getFloat(context.forChild(".speed"));
		int count = count().getInt(context.forChild(".count"));

		for (var viewer : serverLevel.players()) {

			Context viewContext = new Context.Builder(context)
				.withRequired(VIEWER_ENTITY, viewer)
				.build(serverLevel);

			if (viewCondition().test(viewContext.forChild(".view_condition"))) {
				serverLevel.sendParticles(viewer, particle(), force, particle().getType().getOverrideLimiter(), position.x(), position.y(), position.z(), count, spread.x(), spread.y(), spread.z(), speed);
			}

		}

	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		viewCondition().validate(validator.withParams(CONDITION_PARAMETER_SET).forChild(".view_condition"));
		position().validate(validator.forChild(".position"));
		spread().validate(validator.forChild(".spread"));
		speed().validate(validator.forChild(".speed"));
		count().validate(validator.forChild(".count"));
		force().validate(validator.forChild(".force"));
	}

}
