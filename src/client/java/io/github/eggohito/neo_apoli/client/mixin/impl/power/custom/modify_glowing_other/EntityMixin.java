package io.github.eggohito.neo_apoli.client.mixin.impl.power.custom.modify_glowing_other;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.eggohito.neo_apoli.power.custom.ModifyGlowingOtherPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	@Nullable
	public abstract PlayerTeam getTeam();

	@ModifyReturnValue(method = "getTeamColor", at = @At("RETURN"))
	int modifyGlowingColor(int original) {

		Entity renderedEntity = (Entity) (Object) this;
		Team team = this.getTeam();

		try (ModifyGlowingOtherPower.VISITOR) {

			Entity viewer = Minecraft.getInstance().getCameraEntity();
			boolean hasTeamColor = team != null && team.getColor().getColor() != null;

			return ModifyGlowingOtherPower.modifyColor(viewer, renderedEntity, hasTeamColor, original);

		}

	}

}
