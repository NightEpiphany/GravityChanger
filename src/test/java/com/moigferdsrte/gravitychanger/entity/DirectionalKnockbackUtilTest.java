package com.moigferdsrte.gravitychanger.entity;

import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionalKnockbackUtilTest {
    private static final double DELTA = 1.0E-5;

    @Test
    void directionPointsTowardAttackerOnEveryGravityPlane() {
        Vec3 target = new Vec3(4.0, 8.0, 12.0);

        for (Direction gravity : Direction.values()) {
            Vec3 attackerDelta = RotationUtil.vecPlayerToWorld(3.0, 5.0, -4.0, gravity);
            Vec3 direction = DirectionalKnockbackUtil.getDirectionTowardAttacker(
                target,
                target.add(attackerDelta),
                gravity
            );
            Vec3 localDirection = RotationUtil.vecWorldToPlayer(direction, gravity);

            assertEquals(0.6, localDirection.x, DELTA, gravity + " x");
            assertEquals(0.0, localDirection.y, DELTA, gravity + " y");
            assertEquals(-0.8, localDirection.z, DELTA, gravity + " z");
        }
    }

    @Test
    void knockbackMovesAwayAndLiftsAlongLocalUp() {
        for (Direction gravity : Direction.values()) {
            Vec3 directionTowardAttacker = RotationUtil.vecPlayerToWorld(0.6, 0.0, -0.8, gravity);
            Vec3 result = DirectionalKnockbackUtil.applyKnockback(
                Vec3.ZERO,
                directionTowardAttacker,
                0.4,
                true,
                gravity
            );
            Vec3 localResult = RotationUtil.vecWorldToPlayer(result, gravity);

            assertEquals(-0.24, localResult.x, DELTA, gravity + " x");
            assertEquals(0.4, localResult.y, DELTA, gravity + " y");
            assertEquals(0.32, localResult.z, DELTA, gravity + " z");
        }
    }
}
