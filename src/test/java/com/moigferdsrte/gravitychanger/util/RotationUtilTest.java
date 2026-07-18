package com.moigferdsrte.gravitychanger.util;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RotationUtilTest {
    private static final double DELTA = 1.0E-5;

    @Test
    void quaternionPlayerToWorldMatchesDirectionalTransform() {
        Vec3 localVector = new Vec3(0.25, 1.62, -0.75);

        for (Direction direction : Direction.values()) {
            Vec3 expected = RotationUtil.vecPlayerToWorld(localVector, direction);
            Vec3 actual = RotationUtil.vecPlayerToWorld(
                localVector,
                RotationUtil.getEntityRotationQuaternion(direction)
            );

            assertEquals(expected.x, actual.x, DELTA, direction + " x");
            assertEquals(expected.y, actual.y, DELTA, direction + " y");
            assertEquals(expected.z, actual.z, DELTA, direction + " z");
        }
    }

    @Test
    void quaternionWorldToPlayerMatchesDirectionalTransform() {
        Vec3 worldVector = new Vec3(0.25, 1.62, -0.75);

        for (Direction direction : Direction.values()) {
            Vec3 expected = RotationUtil.vecWorldToPlayer(worldVector, direction);
            Vec3 actual = RotationUtil.vecWorldToPlayer(
                worldVector,
                RotationUtil.getEntityRotationQuaternion(direction)
            );

            assertEquals(expected.x, actual.x, DELTA, direction + " x");
            assertEquals(expected.y, actual.y, DELTA, direction + " y");
            assertEquals(expected.z, actual.z, DELTA, direction + " z");
        }
    }

    @Test
    void directionalEyeBoxIsThinAlongGravityAxis() {
        double width = 0.8;
        double thickness = 1.0E-6;

        for (Direction direction : Direction.values()) {
            AABB eyeBox = RotationUtil.makeDirectionalEyeBox(Vec3.ZERO, (float)width, direction);

            assertEquals(direction.getAxis() == Direction.Axis.X ? thickness : width, eyeBox.getXsize(), DELTA, direction + " x");
            assertEquals(direction.getAxis() == Direction.Axis.Y ? thickness : width, eyeBox.getYsize(), DELTA, direction + " y");
            assertEquals(direction.getAxis() == Direction.Axis.Z ? thickness : width, eyeBox.getZsize(), DELTA, direction + " z");
        }
    }

    @Test
    void centerAlignmentPreservesCubeBoundingBoxAcrossGravityChanges() {
        EntityDimensions dimensions = EntityDimensions.scalable(1.0F, 1.0F);
        Vec3 initialPosition = new Vec3(3.0, 7.0, -2.0);
        AABB initialBox = RotationUtil.makeBoxFromDimensions(dimensions, Direction.DOWN, initialPosition);

        for (Direction direction : Direction.values()) {
            Vec3 alignedPosition = RotationUtil.getCenterAlignedPosition(initialBox, dimensions, direction);
            AABB alignedBox = RotationUtil.makeBoxFromDimensions(dimensions, direction, alignedPosition);

            assertBoxEquals(initialBox, alignedBox, direction);
        }
    }

    private static void assertBoxEquals(final AABB expected, final AABB actual, final Direction direction) {
        assertEquals(expected.minX, actual.minX, DELTA, direction + " minX");
        assertEquals(expected.minY, actual.minY, DELTA, direction + " minY");
        assertEquals(expected.minZ, actual.minZ, DELTA, direction + " minZ");
        assertEquals(expected.maxX, actual.maxX, DELTA, direction + " maxX");
        assertEquals(expected.maxY, actual.maxY, DELTA, direction + " maxY");
        assertEquals(expected.maxZ, actual.maxZ, DELTA, direction + " maxZ");
    }
}
