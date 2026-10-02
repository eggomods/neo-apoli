package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_block_harvestable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.eggohito.neo_apoli.power.custom.ModifyBlockHarvestablePower;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviorMixin {

	@WrapOperation(method = "getDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	boolean neo_apoli$modifyHarvestable(Player player, BlockState blockState, Operation<Boolean> original, BlockState mBlockState, Player mPlayer, BlockGetter mBlockGetter, BlockPos mBlockPos) {

		try (ModifyBlockHarvestablePower.VISITOR) {
			return ModifyBlockHarvestablePower.modify(player, mBlockPos, blockState, mBlockGetter.getBlockEntity(mBlockPos), () -> original.call(player, blockState));
		}

	}

}
