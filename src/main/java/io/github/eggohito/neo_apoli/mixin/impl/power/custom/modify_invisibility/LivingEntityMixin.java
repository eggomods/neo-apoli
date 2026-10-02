package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_invisibility;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.eggohito.neo_apoli.power.custom.ModifyInvisibilityPower;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin {

	@WrapOperation(method = "getVisibilityPercent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInvisible()Z"))
	boolean invisibleToProxy(LivingEntity entity, Operation<Boolean> original, @Nullable Entity viewer) {

		try (ModifyInvisibilityPower.VISITOR) {

			if (viewer == null) {
				return original.call(entity);
			}

			else {
				return ModifyInvisibilityPower.modify(entity, viewer, ModifyInvisibilityPower.Instance::isInvisibleTo, () -> original.call(entity));
			}

		}

	}

}
