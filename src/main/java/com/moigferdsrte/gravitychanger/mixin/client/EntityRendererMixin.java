package com.moigferdsrte.gravitychanger.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.moigferdsrte.gravitychanger.client.GravityAnimationEntity;
import com.moigferdsrte.gravitychanger.client.GravityLeashRenderUtil;
import com.moigferdsrte.gravitychanger.client.GravityRenderState;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void gravitychanger$extractGravityDirection(
        final Entity entity,
        final EntityRenderState state,
        final float partialTicks,
        final CallbackInfo ci
    ) {
        GravityRenderState gravityRenderState = (GravityRenderState)state;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        Quaternionf gravityRotation = ((GravityAnimationEntity)entity)
            .gravitychanger$getVisualGravityRotation(gravityDirection);
        gravityRenderState.gravitychanger$setGravityRotation(gravityRotation);
        GravityLeashRenderUtil.updateLeashEndpoints(entity, state, partialTicks, gravityRotation);
    }

    @WrapOperation(
        method = "submit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitLeash(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/entity/state/EntityRenderState$LeashState;)V"
        )
    )
    private void gravitychanger$submitLeashInWorldSpace(
        final SubmitNodeCollector submitNodeCollector,
        final PoseStack poseStack,
        final EntityRenderState.LeashState leashState,
        final Operation<Void> operation,
        final EntityRenderState state
    ) {
        Quaternionf gravityRotation = ((GravityRenderState)state).gravitychanger$getGravityRotation();
        if (RotationUtil.isIdentityRotation(gravityRotation)) {
            operation.call(submitNodeCollector, poseStack, leashState);
            return;
        }

        poseStack.pushPose();
        try {
            poseStack.rotate(new Quaternionf(gravityRotation).conjugate());
            operation.call(submitNodeCollector, poseStack, leashState);
        } finally {
            poseStack.popPose();
        }
    }
}
