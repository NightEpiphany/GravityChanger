package com.moigferdsrte.gravitychanger.client;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CapeAnimationUtilTest {
    private static final float DELTA = 1.0E-5F;

    @Test
    void localVerticalLagOnlyChangesCapeFlap() {
        AvatarRenderState state = new AvatarRenderState();

        CapeAnimationUtil.applyLocalMovement(state, new Vec3(0.0, 1.0, 0.0), 0.0F, 0.0F, 0.0F);

        assertEquals(10.0F, state.capeFlap, DELTA);
        assertEquals(0.0F, state.capeLean, DELTA);
        assertEquals(0.0F, state.capeLean2, DELTA);
    }

    @Test
    void localSideLagOnlyChangesSideLean() {
        AvatarRenderState state = new AvatarRenderState();

        CapeAnimationUtil.applyLocalMovement(state, new Vec3(1.0, 0.0, 0.0), 0.0F, 0.0F, 0.0F);

        assertEquals(0.0F, state.capeFlap, DELTA);
        assertEquals(0.0F, state.capeLean, DELTA);
        assertEquals(-20.0F, state.capeLean2, DELTA);
    }
}
