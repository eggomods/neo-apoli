package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_climbing;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.custom.ModifyClimbingPower;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	LivingEntityMixin(EntityType<?> type, Level world) {
		super(type, world);
	}

	@ModifyExpressionValue(method = "onClimbable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
	boolean modifyClimbing(boolean original) {

		try (ModifyClimbingPower.VISITOR) {
			return original || ModifyClimbingPower.modify(this, Power.Instance::isActive);
		}

	}

	@ModifyReturnValue(method = "isSuppressingSlidingDownLadder", at = @At("RETURN"))
	boolean overrideClimbingHold(boolean original) {

		try (ModifyClimbingPower.VISITOR) {
			return Powers.hasInstances(this, ModifyClimbingPower.Instance.class)
				? ModifyClimbingPower.modify(this, ModifyClimbingPower.Instance::canHold)
				: original;
		}

	}

}
