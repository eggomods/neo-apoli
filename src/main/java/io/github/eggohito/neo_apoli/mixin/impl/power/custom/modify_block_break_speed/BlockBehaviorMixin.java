package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_block_break_speed;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.eggohito.neo_apoli.power.custom.ModifyBlockBreakSpeedPower;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviorMixin {

	@ModifyReturnValue(method = "getDestroyProgress", at = @At("RETURN"))
	float modifyBreakSpeed(float original, BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos) {

		try (ModifyBlockBreakSpeedPower.VISITOR) {
			return ModifyBlockBreakSpeedPower.modify(player, blockPos, blockState, blockGetter.getBlockEntity(blockPos), original);
		}

	}

}
