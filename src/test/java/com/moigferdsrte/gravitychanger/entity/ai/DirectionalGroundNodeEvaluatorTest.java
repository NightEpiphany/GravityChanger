package com.moigferdsrte.gravitychanger.entity.ai;

import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectionalGroundNodeEvaluatorTest {
    private static final double DELTA = 1.0E-5;

    @Test
    void localHorizontalDirectionsStayPerpendicularToGravity() {
        Direction[] localHorizontal = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
        };

        for (Direction gravity : Direction.values()) {
            for (Direction localDirection : localHorizontal) {
                Direction worldDirection = DirectionalGroundNodeEvaluator.localToWorld(localDirection, gravity);
                int dot = worldDirection.getStepX() * gravity.getStepX()
                    + worldDirection.getStepY() * gravity.getStepY()
                    + worldDirection.getStepZ() * gravity.getStepZ();
                assertEquals(0, dot, gravity + " " + localDirection);
            }
        }
    }

    @Test
    void localVerticalAxisMatchesGravity() {
        for (Direction gravity : Direction.values()) {
            Vec3 localDown = RotationUtil.vecPlayerToWorld(0.0, -1.0, 0.0, gravity);
            Vec3 localUp = RotationUtil.vecPlayerToWorld(0.0, 1.0, 0.0, gravity);

            assertVectorEquals(Vec3.atLowerCornerOf(gravity.getUnitVec3i()), localDown, gravity + " down");
            assertVectorEquals(Vec3.atLowerCornerOf(gravity.getOpposite().getUnitVec3i()), localUp, gravity + " up");
        }
    }

    @Test
    void nodeAndEntityPositionsRoundTripForEveryGravityDirection() {
        BlockPos node = new BlockPos(-7, 11, 23);

        for (Direction gravity : Direction.values()) {
            Vec3 entityPosition = DirectionalGroundNodeEvaluator.entityPosition(node, gravity);
            assertEquals(node, DirectionalGroundNodeEvaluator.nodePosition(entityPosition, gravity), gravity.toString());
        }
    }

    @Test
    void closedNodesCannotReenterThePathSearch() {
        Node node = new Node(1, 2, 3);
        node.costMalus = 0.0F;
        assertTrue(DirectionalGroundNodeEvaluator.isUsableNeighbor(node));

        node.closed = true;
        assertFalse(DirectionalGroundNodeEvaluator.isUsableNeighbor(node));
    }

    private static void assertVectorEquals(final Vec3 expected, final Vec3 actual, final String message) {
        assertEquals(expected.x, actual.x, DELTA, message + " x");
        assertEquals(expected.y, actual.y, DELTA, message + " y");
        assertEquals(expected.z, actual.z, DELTA, message + " z");
    }
}
