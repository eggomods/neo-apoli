package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_item_wearable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.eggohito.neo_apoli.power.custom.ModifyItemWearablePower;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@WrapOperation(method = "canEquipWithDispenser", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/equipment/Equippable;canBeEquippedBy(Lnet/minecraft/world/entity/EntityType;)Z"))
	boolean whenDispensed(Equippable equippable, EntityType<?> entityType, Operation<Boolean> original, ItemStack stack) {

		try (ModifyItemWearablePower.VISITOR) {
			return ModifyItemWearablePower.modify(this, stack, equippable.slot(), () -> original.call(equippable, entityType));
		}

	}

}
