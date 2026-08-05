package com.moigferdsrte.gravitychanger.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElytraFlightUtilTest {
    private static final double DELTA = 1.0E-6;

    @Test
    void producesTheSameLocalFlightForEveryGravityDirection() {
        Vec3 localMovement = new Vec3(0.24, -0.18, 0.72);
        Vec3 localLook = new Vec3(0.18, -0.28, 0.94).normalize();
        float leanAngle = 17.0F * (float)(Math.PI / 180.0);
        double gravity = 0.08;
        Vec3 expectedLocal = ElytraFlightUtil.updateMovement(
            localMovement,
            localLook,
            leanAngle,
            gravity,
            Direction.DOWN
        );

        for (Direction direction : Direction.values()) {
            Vec3 worldMovement = RotationUtil.vecPlayerToWorld(localMovement, direction);
            Vec3 worldLook = RotationUtil.vecPlayerToWorld(localLook, direction);
            Vec3 result = ElytraFlightUtil.updateMovement(
                worldMovement,
                worldLook,
                leanAngle,
                gravity,
                direction
            );
            assertVector(expectedLocal, RotationUtil.vecWorldToPlayer(result, direction), direction);
        }
    }

    @Test
    void measuresHorizontalSpeedInTheEntityLocalPlane() {
        Vec3 localMovement = new Vec3(0.3, -1.2, 0.4);
        for (Direction direction : Direction.values()) {
            Vec3 worldMovement = RotationUtil.vecPlayerToWorld(localMovement, direction);
            assertEquals(0.5, ElytraFlightUtil.horizontalDistance(worldMovement, direction), DELTA, direction.getName());
        }
    }

    @Test
    void fallsAlongUpAndWestGravityWhileGliding() {
        Vec3 localLook = new Vec3(0.0, 0.0, 1.0);
        Vec3 upMovement = ElytraFlightUtil.updateMovement(
            Vec3.ZERO,
            RotationUtil.vecPlayerToWorld(localLook, Direction.UP),
            0.0F,
            0.08,
            Direction.UP
        );
        Vec3 westMovement = ElytraFlightUtil.updateMovement(
            Vec3.ZERO,
            RotationUtil.vecPlayerToWorld(localLook, Direction.WEST),
            0.0F,
            0.08,
            Direction.WEST
        );

        assertTrue(upMovement.y > 0.0, "UP gravity must make an elytra fall toward world up");
        assertTrue(westMovement.x < 0.0, "WEST gravity must make an elytra fall toward world west");
    }

    @Test
    void appliesFireworkBoostInTheEntityLocalFrame() {
        Vec3 localMovement = new Vec3(0.2, -0.1, 0.4);
        Vec3 localLook = new Vec3(0.0, 0.6, 0.8);
        Vec3 expected = ElytraFlightUtil.applyFireworkBoost(localMovement, localLook, Direction.DOWN);

        for (Direction direction : Direction.values()) {
            Vec3 result = ElytraFlightUtil.applyFireworkBoost(
                RotationUtil.vecPlayerToWorld(localMovement, direction),
                RotationUtil.vecPlayerToWorld(localLook, direction),
                direction
            );
            assertVector(expected, RotationUtil.vecWorldToPlayer(result, direction), direction);
        }
    }

    @Test
    void boostsAgainstUpAndWestGravityWhenLookingLocallyUp() {
        Vec3 localUp = new Vec3(0.0, 1.0, 0.0);
        Vec3 upBoost = ElytraFlightUtil.applyFireworkBoost(
            Vec3.ZERO,
            RotationUtil.vecPlayerToWorld(localUp, Direction.UP),
            Direction.UP
        );
        Vec3 westBoost = ElytraFlightUtil.applyFireworkBoost(
            Vec3.ZERO,
            RotationUtil.vecPlayerToWorld(localUp, Direction.WEST),
            Direction.WEST
        );

        assertTrue(upBoost.y < 0.0, "UP gravity local ascent must boost toward world down");
        assertTrue(westBoost.x > 0.0, "WEST gravity local ascent must boost toward world east");
    }

    private static void assertVector(final Vec3 expected, final Vec3 actual, final Direction direction) {
        assertEquals(expected.x, actual.x, DELTA, direction.getName() + " x");
        assertEquals(expected.y, actual.y, DELTA, direction.getName() + " y");
        assertEquals(expected.z, actual.z, DELTA, direction.getName() + " z");
    }
}
