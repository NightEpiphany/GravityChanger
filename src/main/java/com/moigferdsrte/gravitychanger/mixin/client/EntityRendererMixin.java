package com.moigferdsrte.gravitychanger.mixin.client;

import com.moigferdsrte.gravitychanger.client.GravityRenderState;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
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
        ((GravityRenderState)state).gravitychanger$setGravityDirection(GravityDirectionUtil.getGravityDirection(entity));
    }
}
