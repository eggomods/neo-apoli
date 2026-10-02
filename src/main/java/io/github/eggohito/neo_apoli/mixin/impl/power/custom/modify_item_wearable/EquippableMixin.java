package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_item_wearable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.eggohito.neo_apoli.power.custom.ModifyItemWearablePower;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Equippable.class)
public abstract class EquippableMixin {

	@Shadow
	public abstract EquipmentSlot slot();

	@WrapOperation(method = "swapWithEquipmentSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/equipment/Equippable;canBeEquippedBy(Lnet/minecraft/world/entity/EntityType;)Z"))
	boolean onSwap(Equippable equippable, EntityType<?> entityType, Operation<Boolean> original, ItemStack stack, Player player) {

		try (ModifyItemWearablePower.VISITOR) {
			return ModifyItemWearablePower.modify(player, stack, this.slot(), () -> original.call(equippable, entityType));
		}

	}

}
