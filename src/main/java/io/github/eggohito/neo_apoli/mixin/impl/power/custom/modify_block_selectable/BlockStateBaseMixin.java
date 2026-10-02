package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_block_selectable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.power.custom.ModifyBlockSelectablePower;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin extends StateHolder<Block, BlockState> {

	@Shadow
	protected abstract BlockState asState();

	BlockStateBaseMixin(Block owner, Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap, MapCodec<BlockState> codec) {
		super(owner, propertyMap, codec);
	}

	@ModifyReturnValue(method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("RETURN"))
	VoxelShape neo_apoli$modifySelectableShape(VoxelShape original, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collision) {

		if (!MiscUtil.collisionHasEntity(collision)) {
			return original;
		}

		try (ModifyBlockSelectablePower.VISITOR) {

			if (ModifyBlockSelectablePower.shouldBeEmpty(MiscUtil.getEntityFromCollision(collision), blockPos, this.asState(), blockGetter.getBlockEntity(blockPos))) {
				return Shapes.empty();
			}

			else {
				return original;
			}

		}

	}

}
