package com.moigferdsrte.gravitychanger.compat.bedrockify;

import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BedrockifyFlyingMomentumCompatTest {
    private static final double DELTA = 1.0E-6;
    private static final Vec3 MOVEMENT = new Vec3(1.25, -2.5, 3.75);

    @Test
    void clearingHorizontalMomentumPreservesOnlyLocalVerticalMovement() {
        for (Direction gravityDirection : Direction.values()) {
            Vec3 originalLocal = RotationUtil.vecWorldToPlayer(MOVEMENT, gravityDirection);
            Vec3 result = BedrockifyFlyingMomentumCompat.clearLocalHorizontal(MOVEMENT, gravityDirection);
            Vec3 resultLocal = RotationUtil.vecWorldToPlayer(result, gravityDirection);

            assertEquals(0.0, resultLocal.x, DELTA, gravityDirection.getName());
            assertEquals(originalLocal.y, resultLocal.y, DELTA, gravityDirection.getName());
            assertEquals(0.0, resultLocal.z, DELTA, gravityDirection.getName());
        }
    }

    @Test
    void clearingVerticalMomentumPreservesLocalHorizontalMovement() {
        for (Direction gravityDirection : Direction.values()) {
            Vec3 originalLocal = RotationUtil.vecWorldToPlayer(MOVEMENT, gravityDirection);
            Vec3 result = BedrockifyFlyingMomentumCompat.clearLocalVertical(MOVEMENT, gravityDirection);
            Vec3 resultLocal = RotationUtil.vecWorldToPlayer(result, gravityDirection);

            assertEquals(originalLocal.x, resultLocal.x, DELTA, gravityDirection.getName());
            assertEquals(0.0, resultLocal.y, DELTA, gravityDirection.getName());
            assertEquals(originalLocal.z, resultLocal.z, DELTA, gravityDirection.getName());
        }
    }
}
