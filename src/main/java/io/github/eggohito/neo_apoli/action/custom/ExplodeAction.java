package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.custom.ConstantCondition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextParams;
import io.github.eggohito.neo_apoli.context.ContextUser;
import io.github.eggohito.neo_apoli.context.parameter.BlockContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.EntityContextParameter;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.floats.ConstantFloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.registry.NeoApoliParticleTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ExplodeAction(Condition damageableCondition, Condition destructibleCondition, Display display, Property property, Vec3Provider position, Optional<EntityProvider> emitter) implements Action {

	public static final BlockContextParameter EXPLODED_BLOCK = new BlockContextParameter(NeoApoli.id("exploded_block"));
	public static final EntityContextParameter EMITTER_ENTITY = new EntityContextParameter(NeoApoli.id("emitter_entity"));
	public static final EntityContextParameter EXPLODED_ENTITY = new EntityContextParameter(NeoApoli.id("exploded_entity"));

	public static final ContextParams DAMAGEABLE_PARAMETER_SET = new ContextParams.Builder()
		.required(EXPLODED_ENTITY)
		.optional(EMITTER_ENTITY)
		.build();
	public static final ContextParams DESTRUCTIBLE_PARAMETER_SET = new ContextParams.Builder()
		.required(EXPLODED_BLOCK)
		.build();
	public static final ContextParams KNOCKBACK_MULTIPLIER_PARAMETER_SET = new ContextParams.Builder()
		.required(EXPLODED_ENTITY)
		.optional(EMITTER_ENTITY)
		.build();

	public static final MapCodec<ExplodeAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.optionalFieldOf("damageable_condition", new ConstantCondition(true)).forGetter(ExplodeAction::damageableCondition),
		Condition.CODEC.optionalFieldOf("destructible_condition", new ConstantCondition(true)).forGetter(ExplodeAction::destructibleCondition),
		Display.CODEC.forGetter(ExplodeAction::display),
		Property.CODEC.forGetter(ExplodeAction::property),
		Vec3Provider.CODEC.fieldOf("position").forGetter(ExplodeAction::position),
		EntityProvider.CODEC.optionalFieldOf("emitter").forGetter(ExplodeAction::emitter)
	).apply(instance, ExplodeAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ExplodeAction> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC, ExplodeAction::damageableCondition,
		Condition.STREAM_CODEC, ExplodeAction::destructibleCondition,
		Display.STREAM_CODEC, ExplodeAction::display,
		Property.STREAM_CODEC, ExplodeAction::property,
		Vec3Provider.STREAM_CODEC, ExplodeAction::position,
		ByteBufCodecs.optional(EntityProvider.STREAM_CODEC), ExplodeAction::emitter,
		ExplodeAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.EXPLODE;
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

		Context powerContext = context.forChild(".power");
		float power = property().power().getFloat(powerContext);

		if (powerContext.hasProblems()) {
			return;
		}

		Context createFireContext = context.forChild(".create_fire");
		boolean createFire = property().createFire().getBoolean(createFireContext);

		if (createFireContext.hasProblems()) {
			return;
		}

		Entity emitter = emitter()
			.flatMap(self -> self.getValue(context.forChild(".entity")))
			.orElse(null);
		DamageCalculator calculator = new DamageCalculator(emitter, context);

		ServerExplosion explosion = new ServerExplosion(serverLevel, emitter, Explosion.getDefaultDamageSource(serverLevel, emitter), calculator, position, power, createFire, property().blockInteraction());
		explosion.explode();

		ParticleOptions particle = explosion.isSmall()
			? display().smallParticle()
			: display().largeParticle();

		for (var player : serverLevel.players()) {

			if (player.distanceToSqr(position) >= 4096) {
				continue;
			}

			Optional<Vec3> knockback = Optional.ofNullable(explosion.getHitPlayers().get(player));
			player.connection.send(new ClientboundExplodePacket(position, knockback, particle, display().sound()));

		}

	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		damageableCondition().validate(validator.withParams(DAMAGEABLE_PARAMETER_SET).forChild(".damageable_condition"));
		destructibleCondition().validate(validator.withParams(DESTRUCTIBLE_PARAMETER_SET).forChild(".destructible_condition"));
		property().validate(validator.forChild(".property"));
		position().validate(validator.forChild(".position"));
		emitter().ifPresent(emitter -> emitter.validate(validator.forChild(".emitter")));
	}

	public record Property(Explosion.BlockInteraction blockInteraction, FloatProvider power, FloatProvider knockbackMultiplier, BooleanProvider createFire) implements ContextUser {

		public static final MapCodec<Property> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			NeoApoliCodecs.BLOCK_INTERACTION.fieldOf("block_interaction").forGetter(Property::blockInteraction),
			FloatProvider.CODEC.fieldOf("power").forGetter(Property::power),
			FloatProvider.CODEC.optionalFieldOf("knockback_multiplier", new ConstantFloatProvider(1.0F)).forGetter(Property::knockbackMultiplier),
			BooleanProvider.CODEC.optionalFieldOf("create_fire", new ConstantBooleanProvider(true)).forGetter(Property::createFire)
		).apply(instance, Property::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Property> STREAM_CODEC = StreamCodec.composite(
			NeoApoliStreamCodecs.BLOCK_INTERACTION, Property::blockInteraction,
			FloatProvider.STREAM_CODEC, Property::power,
			FloatProvider.STREAM_CODEC, Property::knockbackMultiplier,
			BooleanProvider.STREAM_CODEC, Property::createFire,
			Property::new
		);

		@Override
		public void validate(Context.Validator validator) {
			ContextUser.super.validate(validator);
			power().validate(validator.forChild(".power"));
			knockbackMultiplier().validate(validator.withParams(KNOCKBACK_MULTIPLIER_PARAMETER_SET).forChild(".knockback_multiplier"));
			createFire().validate(validator.forChild(".create_fire"));
		}

	}

	public record Display(Holder<SoundEvent> sound, ParticleOptions smallParticle, ParticleOptions largeParticle) {

		public static final MapCodec<Display> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			SoundEvent.CODEC.optionalFieldOf("sound", SoundEvents.GENERIC_EXPLODE).forGetter(Display::sound),
			NeoApoliCodecs.PARTICLE_OPTIONS.optionalFieldOf("small_particle", ParticleTypes.EXPLOSION).forGetter(Display::smallParticle),
			NeoApoliCodecs.PARTICLE_OPTIONS.optionalFieldOf("large_particle", ParticleTypes.EXPLOSION_EMITTER).forGetter(Display::largeParticle)
		).apply(instance, Display::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Display> STREAM_CODEC = StreamCodec.composite(
			SoundEvent.STREAM_CODEC, Display::sound,
			NeoApoliParticleTypes.OPTIONS_STREAM_CODEC, Display::smallParticle,
			NeoApoliParticleTypes.OPTIONS_STREAM_CODEC, Display::largeParticle,
			Display::new
		);

	}

	@AllArgsConstructor
	public final class DamageCalculator extends ExplosionDamageCalculator {

		@Nullable
		private final Entity emitter;
		@NotNull
		private final Context context;

		@Override
		public boolean shouldBlockExplode(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, float power) {

			Context destructibleContext = new Context.Builder(context)
				.withRequired(EXPLODED_BLOCK, new CachedBlock(pos, state, reader.getBlockEntity(pos)))
				.build(context.level());

			return ExplodeAction.this.destructibleCondition().test(destructibleContext.forChild(".destructible_condition"));

		}

		@Override
		public boolean shouldDamageEntity(Explosion explosion, Entity entity) {

			Context damageableContext = new Context.Builder(context)
				.withNullable(EMITTER_ENTITY, emitter)
				.withNullable(EXPLODED_ENTITY, entity)
				.build(context.level());

			return ExplodeAction.this.damageableCondition().test(damageableContext.forChild(".damageable_condition"));

		}

		@Override
		public float getKnockbackMultiplier(Entity entity) {

			Context knockbackContext = new Context.Builder(context)
				.withNullable(EMITTER_ENTITY, emitter)
				.withRequired(EXPLODED_ENTITY, entity)
				.build(context.level());

			return ExplodeAction.this.property().knockbackMultiplier().getFloat(knockbackContext.forChild(".knockback_multiplier"));

		}

	}

}
