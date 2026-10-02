package io.github.eggohito.neo_apoli.client.mixin.impl.power.custom.modify_invisibility;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.eggohito.neo_apoli.power.custom.ModifyInvisibilityPower;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> {

	LivingEntityRendererMixin(EntityRendererProvider.Context context) {
		super(context);
	}

	@ModifyExpressionValue(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z"))
	boolean showOrHideOutlineWhenInvisible(boolean original, LivingEntity renderedEntity) {

		try (ModifyInvisibilityPower.VISITOR) {

			if (original) {
				return !ModifyInvisibilityPower.modify(renderedEntity, Minecraft.getInstance().getCameraEntity(), ModifyInvisibilityPower.RENDER_OUTLINE.negate(), () -> false);
			}

			else {
				return false;
			}

		}

	}

	@WrapWithCondition(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/EntityRenderState;FF)V"))
	boolean showOrHideArmorFeatureWhenInvisible(RenderLayer<S, M> renderLayer, PoseStack poseStack, MultiBufferSource bufferSource, int light, EntityRenderState state, float yRot, float xRot) {

		try (ModifyInvisibilityPower.VISITOR) {

			if (renderLayer instanceof HumanoidArmorLayer) {
				return ModifyInvisibilityPower.modify(state.neo_apoli$getEntity(), Minecraft.getInstance().getCameraEntity(), ModifyInvisibilityPower.RENDER_ARMOR.negate(), () -> true);
			}

			else {
				return true;
			}

		}

	}

}
