package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_jump;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.eggohito.neo_apoli.power.custom.ModifyJumpPower;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@ModifyReturnValue(method = "getJumpPower()F", at = @At("RETURN"))
	float modifyJump(float original) {

		try (ModifyJumpPower.VISITOR) {
			return ModifyJumpPower.modify(thisAsLiving(), original);
		}

	}

	@Unique
	private LivingEntity thisAsLiving() {
		return (LivingEntity) (Object) this;
	}

}
