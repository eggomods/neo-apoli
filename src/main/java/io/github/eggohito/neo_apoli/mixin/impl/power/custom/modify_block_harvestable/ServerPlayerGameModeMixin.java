package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_block_harvestable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.eggohito.neo_apoli.power.custom.ModifyBlockHarvestablePower;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

	@Shadow
	protected ServerLevel level;

	@WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	boolean neo_apoli$modifyHarvestable(ServerPlayer serverPlayer, BlockState blockState, Operation<Boolean> original, BlockPos mBlockPos, @Local BlockEntity blockEntity) {

		try (ModifyBlockHarvestablePower.VISITOR) {
			return ModifyBlockHarvestablePower.modify(serverPlayer, mBlockPos, blockState, blockEntity, () -> original.call(serverPlayer, blockState));
		}

	}

}
