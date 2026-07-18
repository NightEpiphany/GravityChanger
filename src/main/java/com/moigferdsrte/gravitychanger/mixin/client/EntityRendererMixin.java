package com.moigferdsrte.gravitychanger.mixin.client;

import com.moigferdsrte.gravitychanger.client.GravityAnimationEntity;
import com.moigferdsrte.gravitychanger.client.GravityRenderState;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
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
        GravityRenderState gravityRenderState = (GravityRenderState)state;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        gravityRenderState.gravitychanger$setGravityRotation(
            ((GravityAnimationEntity)entity).gravitychanger$getVisualGravityRotation(gravityDirection)
        );
    }
}
