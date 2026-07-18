package com.moigferdsrte.gravitychanger.client;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class CapeAnimationUtil {
    private CapeAnimationUtil() {
    }

    public static void applyLocalMovement(
        final AvatarRenderState state,
        final Vec3 localCloakDelta,
        final float bodyRotation,
        final float bob,
        final float walkDistance
    ) {
        double forwardX = Mth.sin(bodyRotation * (float)(Math.PI / 180.0));
        double forwardZ = -Mth.cos(bodyRotation * (float)(Math.PI / 180.0));
        state.capeFlap = Mth.clamp((float)localCloakDelta.y * 10.0F, -6.0F, 32.0F);
        state.capeLean = (float)(localCloakDelta.x * forwardX + localCloakDelta.z * forwardZ) * 100.0F;
        state.capeLean = Mth.clamp(state.capeLean * (1.0F - state.fallFlyingScale()), 0.0F, 150.0F);
        state.capeLean2 = (float)(localCloakDelta.x * forwardZ - localCloakDelta.z * forwardX) * 100.0F;
        state.capeLean2 = Mth.clamp(state.capeLean2, -20.0F, 20.0F);
        state.capeFlap += Mth.sin(walkDistance * 6.0F) * 32.0F * bob;
    }
}
