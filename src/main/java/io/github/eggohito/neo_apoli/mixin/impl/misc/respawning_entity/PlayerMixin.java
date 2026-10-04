package io.github.eggohito.neo_apoli.mixin.impl.misc.respawning_entity;

import io.github.eggohito.neo_apoli.duck.internal.RespawningEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements RespawningEntity {

	PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Unique
	private boolean neo_apoli$isRespawning = false;

	@Override
	public boolean neo_apoli$isRespawning() {
		return this.neo_apoli$isRespawning;
	}

	@Override
	public void neo_apoli$setRespawning(boolean value) {
		this.neo_apoli$isRespawning = value;
	}

}
