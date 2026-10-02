package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_player_spawn;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.power.custom.ModifyPlayerSpawnPower;
import io.github.eggohito.neo_apoli.power.custom.misc.PrioritizedPower;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

	@ModifyExpressionValue(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;"))
	TeleportTransition onRespawn(TeleportTransition original, ServerPlayer player) {

		if (player.getRespawnConfig() != null && !original.missingRespawnBlock()) {
			return original;
		}

		var instances = new PrioritizedPower.InstanceCollection<>(player, ModifyPlayerSpawnPower.Instance.class);
		ModifyPlayerSpawnPower.Instance firstInstance = null;

		for (var instance : instances) {

			Context context = instance.createHolderContext(player);
			CompletableFuture<TeleportTransition> respawnLocation = instance.getRespawnLocation();

			if (instance.isActive(context)) {

				if (respawnLocation.isDone()) {
					return respawnLocation.join();
				}

				else if (firstInstance == null){
					firstInstance = instance;
				}

			}

		}

		if (firstInstance != null) {
			return firstInstance.getOrFindRespawnLocation(player).join();
		}

		else {
			return original;
		}

	}

}
