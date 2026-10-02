package io.github.eggohito.neo_apoli.client.mixin.impl.power.custom.phasing;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.custom.PhasingPower;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

	@ModifyExpressionValue(method = "renderScreenEffect", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;noPhysics:Z", opcode = Opcodes.GETFIELD))
	private static boolean preventBlockingEffectWhenPhasing(boolean original, @Local Player player) {

		try (PhasingPower.VISITOR) {
			CachedBlock viewBlocking = MiscUtil.getViewBlocking(player);
			return original
				|| (viewBlocking != null && PhasingPower.doesApply(player, viewBlocking, Power.Instance::isActive));
		}

	}

}
