package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_air_speed;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.eggohito.neo_apoli.power.custom.ModifyAirSpeedPower;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	protected LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@ModifyReturnValue(method = "getFlyingSpeed", at = @At("RETURN"))
	protected float modifyAirSpeed(float original) {

		try (ModifyAirSpeedPower.VISITOR) {
			return ModifyAirSpeedPower.modify(this, original);
		}

	}

}
