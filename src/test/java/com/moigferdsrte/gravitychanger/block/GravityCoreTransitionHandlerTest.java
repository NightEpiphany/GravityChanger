package com.moigferdsrte.gravitychanger.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.EntityDimensions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GravityCoreTransitionHandlerTest {
    private static final double DELTA = 1.0E-6;
    private static final BlockPos CORE_POS = new BlockPos(0, 0, 0);

    @Test
    void detectsEveryTangentialEdgeForEveryGravityDirection() {
        for (Direction gravity : Direction.values()) {
            for (Direction edge : Direction.values()) {
                if (edge.getAxis() == gravity.getAxis()) {
                    continue;
                }

                Vec3 position = nearEdge(edge);
                Vec3 movement = directionVector(edge).scale(0.08);

                assertEquals(
                    edge,
                    GravityCoreTransitionHandler.findTransitionEdge(position, movement, CORE_POS, gravity),
                    gravity + " gravity toward " + edge
                );
            }
        }
    }

    @Test
    void doesNotTransitionWhileStationaryOrFarFromEdge() {
        assertNull(GravityCoreTransitionHandler.findTransitionEdge(
            new Vec3(0.5, 1.0, 0.05),
            Vec3.ZERO,
            CORE_POS,
            Direction.DOWN
        ));
        assertNull(GravityCoreTransitionHandler.findTransitionEdge(
            new Vec3(0.5, 1.0, 0.5),
            new Vec3(0.0, 0.0, -0.08),
            CORE_POS,
            Direction.DOWN
        ));
    }

    @Test
    void diagonalMovementChoosesTheFirstEdgeItWillReach() {
        Direction edge = GravityCoreTransitionHandler.findTransitionEdge(
            new Vec3(0.95, 1.0, 0.88),
            new Vec3(0.08, 0.0, 0.10),
            CORE_POS,
            Direction.DOWN
        );

        assertEquals(Direction.EAST, edge);
    }

    @Test
    void rotatesForwardMovementAroundTheSharedEdge() {
        Vec3 rotated = GravityCoreTransitionHandler.rotateAroundEdge(
            directionVector(Direction.NORTH),
            Direction.DOWN,
            Direction.NORTH
        );

        assertVectorEquals(directionVector(Direction.DOWN), rotated);
    }

    @Test
    void rotationMapsOldGravityToNewGravityAndPreservesSharedEdgeAxis() {
        Vec3 oldGravity = GravityCoreTransitionHandler.rotateAroundEdge(
            directionVector(Direction.DOWN),
            Direction.DOWN,
            Direction.EAST
        );
        Vec3 sharedEdge = GravityCoreTransitionHandler.rotateAroundEdge(
            directionVector(Direction.NORTH),
            Direction.DOWN,
            Direction.EAST
        );

        assertVectorEquals(directionVector(Direction.WEST), oldGravity);
        assertVectorEquals(directionVector(Direction.NORTH), sharedEdge);
    }

    @Test
    void placesTheNewFootAnchorJustOutsideTheTargetFace() {
        Vec3 target = GravityCoreTransitionHandler.getTargetPosition(
            new Vec3(0.5, 1.0, 0.08),
            CORE_POS,
            Direction.NORTH
        );

        assertEquals(0.5, target.x, DELTA);
        assertEquals(1.0, target.y, DELTA);
        assertEquals(-5.0E-7, target.z, DELTA);
    }

    @Test
    void centersFallTransitionOnTheTargetFace() {
        Vec3 target = GravityCoreTransitionHandler.getFaceCenterPosition(
            EntityDimensions.scalable(0.6F, 1.8F),
            CORE_POS,
            Direction.SOUTH,
            Direction.DOWN
        );

        assertEquals(0.5, target.x, DELTA);
        assertEquals(0.8, target.y, DELTA);
        assertEquals(1.0 + 5.0E-7, target.z, DELTA);
    }

    private static Vec3 nearEdge(final Direction direction) {
        double coordinate = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0.94 : 0.06;
        return switch (direction.getAxis()) {
            case X -> new Vec3(coordinate, 0.5, 0.5);
            case Y -> new Vec3(0.5, coordinate, 0.5);
            case Z -> new Vec3(0.5, 0.5, coordinate);
        };
    }

    private static Vec3 directionVector(final Direction direction) {
        return new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    private static void assertVectorEquals(final Vec3 expected, final Vec3 actual) {
        assertEquals(expected.x, actual.x, DELTA);
        assertEquals(expected.y, actual.y, DELTA);
        assertEquals(expected.z, actual.z, DELTA);
    }
}
