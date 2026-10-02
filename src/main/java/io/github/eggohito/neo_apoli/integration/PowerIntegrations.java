package io.github.eggohito.neo_apoli.integration;

import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.event.KeyStateEvents;
import io.github.eggohito.neo_apoli.key.KeyState;
import io.github.eggohito.neo_apoli.power.custom.InventoryPower;
import io.github.eggohito.neo_apoli.power.custom.ModifyElytraFlightPower;
import io.github.eggohito.neo_apoli.power.custom.TogglePower;
import io.github.eggohito.neo_apoli.power.custom.misc.PrioritizedPower;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;

public final class PowerIntegrations {

	public static void init() {
		ServerPlayerEvents.AFTER_RESPAWN.register(Powers.ID, PowerIntegrations::onRespawnCallback);
		EntityElytraEvents.ALLOW.register(PowerIntegrations::allowPowerElytraFlight);
		EntityElytraEvents.CUSTOM.register(PowerIntegrations::onPowerElytraFlight);
		KeyStateEvents.HELD.register(PowerIntegrations::toggleOnKeyHeld);
		KeyStateEvents.HELD.register(PowerIntegrations::openInventoryOnKeyHeld);
	}

	private static void onRespawnCallback(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
		Powers.getAllInstances(newPlayer).forEach(instance -> instance.onRespawned(newPlayer));
	}

	private static void openInventoryOnKeyHeld(Player player, KeyState previous, KeyState current) {

		for (var instance : new PrioritizedPower.InstanceCollection<>(player, InventoryPower.Instance.class)) {

			Context context = instance.createHolderContext(player);

			if (instance.shouldOpen(context, previous, current) && instance.open(player)) {
				break;
			}

		}

	}

	private static void toggleOnKeyHeld(Player player, KeyState previous, KeyState current) {

		for (var instance : Powers.getInstances(player, TogglePower.Instance.class)) {

			Context context = instance.createHolderContext(player);

			if (instance.shouldToggle(context, previous, current)) {
				instance.toggle(player, context);
			}

		}

	}

	private static boolean onPowerElytraFlight(LivingEntity entity, boolean tickElytra) {

		try (ModifyElytraFlightPower.VISITOR) {

			boolean allow = ModifyElytraFlightPower.modify(entity, () -> false);

			if (allow && tickElytra) {
				entity.gameEvent(GameEvent.ELYTRA_GLIDE);
			}

			return allow;

		}

	}

	private static boolean allowPowerElytraFlight(LivingEntity entity) {

		try (ModifyElytraFlightPower.VISITOR) {
			return ModifyElytraFlightPower.modify(entity, () -> true);
		}

	}

}
