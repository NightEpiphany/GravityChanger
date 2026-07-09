package com.moigferdsrte.gravitychanger.mixin.client;

import com.moigferdsrte.gravitychanger.client.GravityRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin {
    @Redirect(
        method = "renderToTexture",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"
        )
    )
    private void gravitychanger$submitGuiEntityWithoutGravityRotation(
        final EntityRenderDispatcher dispatcher,
        final EntityRenderState renderState,
        final CameraRenderState camera,
        final double x,
        final double y,
        final double z,
        final PoseStack poseStack,
        final SubmitNodeCollector submitNodeCollector
    ) {
        GravityRenderContext.setRenderingGuiEntity(true);
        try {
            dispatcher.submit(renderState, camera, x, y, z, poseStack, submitNodeCollector);
        } finally {
            GravityRenderContext.setRenderingGuiEntity(false);
        }
    }
}
