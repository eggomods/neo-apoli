package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.BlockContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.DirectionContextParameter;
import io.github.eggohito.neo_apoli.exception.PosOutOfBoundsException;
import io.github.eggohito.neo_apoli.exception.PosUnloadedException;
import io.github.eggohito.neo_apoli.mixin.access.AABBAccessor;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.custom.misc.CallbackPower;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record CallbackProjectileLandPower(Optional<Condition> activeCondition, Action action) implements CallbackPower {

	public static final BlockContextParameter LANDED_ON_BLOCK = new BlockContextParameter(NeoApoli.id("landed_on_block"));
	public static final DirectionContextParameter LANDED_ON_SIDE = new DirectionContextParameter(NeoApoli.id("landed_on_side"));

	public static final MapCodec<CallbackProjectileLandPower> CODEC = CallbackPower.codec(CallbackProjectileLandPower::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackProjectileLandPower> STREAM_CODEC = CallbackPower.streamCodec(CallbackProjectileLandPower::new);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.CALLBACK_PROJECTILE_LAND;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<CallbackProjectileLandPower> {

		protected Instance(@NotNull CallbackProjectileLandPower power) {
			super(power);
		}

		public Context createContext(Entity holder, Entity owner, Projectile projectile, HitResult result) {

			Level level = projectile.level();
			Vec3 pos = result.getLocation();

			Entity target;
			BlockPos blockPos;
			Direction side;

			switch (result) {
				case EntityHitResult entityResult -> {

					target = entityResult.getEntity();
					Vec3 end = target.position();

					double dx = end.x() - pos.x();
					double dy = end.y() - pos.y();
					double dz = end.z() - pos.z();

					blockPos = BlockPos.containing(pos);
					side = AABBAccessor.callGetDirection(target.getBoundingBox(), pos, new double[]{1.0}, null, dx, dy, dz);

				}
				case BlockHitResult blockResult -> {
					target = null;
					blockPos = blockResult.getBlockPos();
					side = blockResult.getDirection();
				}
				default -> {
					target = null;
					blockPos = BlockPos.containing(pos);
					side = null;
				}
			}

			return this.createHolderContextBuilder(holder)
				.withRequired(LANDED_ON_BLOCK, CachedBlock.fromLoadedPos(level, blockPos))
				.withNullable(LANDED_ON_SIDE, side)
				.withRequired(NeoApoliContextParameters.PROJECTILE_ENTITY, projectile)
				.withNullable(NeoApoliContextParameters.ACTOR_ENTITY, owner)
				.withNullable(NeoApoliContextParameters.TARGET_ENTITY, target)
				.build(level);

		}

		public void execute(Context context) {
			power.action().execute(context.forChild(".action"));
		}

	}

	public static void executeAsOwner(Entity owner, Projectile projectile, HitResult result) {
		execute(owner, owner, projectile, result);
	}

	public static void executeAsProjectile(Entity owner, Projectile projectile, HitResult result) {
		execute(projectile, owner, projectile, result);
	}

	public static void execute(Entity powerHolder, Entity owner, Projectile projectile, HitResult result) {

		for (var instance : Powers.getInstances(powerHolder, Instance.class)) {

			try {

				Context context = instance.createContext(powerHolder, owner, projectile, result);

				if (instance.isActive(context)) {
					instance.execute(context);
				}

			}

			catch (PosUnloadedException | PosOutOfBoundsException ignored) {
				//  No-op; just need to safe error
			}

		}

	}

}
