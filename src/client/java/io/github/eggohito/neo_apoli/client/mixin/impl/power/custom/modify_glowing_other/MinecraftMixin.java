package io.github.eggohito.neo_apoli.client.mixin.impl.power.custom.modify_glowing_other;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.eggohito.neo_apoli.power.custom.ModifyGlowingOtherPower;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@Shadow
	public abstract @Nullable Entity getCameraEntity();

	@ModifyExpressionValue(method = "shouldEntityAppearGlowing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isCurrentlyGlowing()Z"))
	boolean makeOthersGlow(boolean original, Entity entity) {

		try (ModifyGlowingOtherPower.VISITOR) {
			return original || ModifyGlowingOtherPower.modifyGlowing(this.getCameraEntity(), entity);
		}

	}

}
