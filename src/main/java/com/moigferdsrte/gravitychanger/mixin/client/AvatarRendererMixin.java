package com.moigferdsrte.gravitychanger.mixin.client;

import com.moigferdsrte.gravitychanger.client.CapeAnimationUtil;
import com.moigferdsrte.gravitychanger.client.GravityAnimationEntity;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(method = "extractCapeState", at = @At("TAIL"))
    private void gravitychanger$extractCapeStateInLocalGravitySpace(
        final Avatar entity,
        final AvatarRenderState state,
        final float partialTicks,
        final CallbackInfo ci
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        ClientAvatarState clientState = ((ClientAvatarEntity)entity).avatarState();
        Vec3 cloakPosition = new Vec3(
            clientState.getInterpolatedCloakX(partialTicks),
            clientState.getInterpolatedCloakY(partialTicks),
            clientState.getInterpolatedCloakZ(partialTicks)
        );
        Vec3 entityPosition = new Vec3(
            Mth.lerp(partialTicks, entity.xo, entity.getX()),
            Mth.lerp(partialTicks, entity.yo, entity.getY()),
            Mth.lerp(partialTicks, entity.zo, entity.getZ())
        );
        Quaternionf visualRotation = ((GravityAnimationEntity)entity)
            .gravitychanger$getVisualGravityRotation(gravityDirection);
        Vec3 localCloakDelta = RotationUtil.vecWorldToPlayer(
            cloakPosition.subtract(entityPosition),
            visualRotation
        );
        float bodyRotation = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        CapeAnimationUtil.applyLocalMovement(
            state,
            localCloakDelta,
            bodyRotation,
            clientState.getInterpolatedBob(partialTicks),
            clientState.getInterpolatedWalkDistance(partialTicks)
        );
    }
}
