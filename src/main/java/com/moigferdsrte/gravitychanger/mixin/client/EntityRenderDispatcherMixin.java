package com.moigferdsrte.gravitychanger.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moigferdsrte.gravitychanger.client.GravityRenderContext;
import com.moigferdsrte.gravitychanger.client.GravityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(
        method = "submit",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void gravitychanger$rotateEntityModel(
        final EntityRenderState renderState,
        final CameraRenderState camera,
        final double x,
        final double y,
        final double z,
        final PoseStack poseStack,
        final SubmitNodeCollector submitNodeCollector,
        final CallbackInfo ci
    ) {
        if (GravityRenderContext.isRenderingGuiEntity()) {
            return;
        }

        poseStack.mulPose(((GravityRenderState)renderState).gravitychanger$getGravityRotation());
    }
}
