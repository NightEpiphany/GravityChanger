package com.moigferdsrte.gravitychanger.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GravityDirectionUtilTest {
    private static final double DELTA = 1.0E-12;

    @Test
    void calculatesStrengthScaleRelativeToDefaultGravity() {
        assertEquals(1.0, GravityDirectionUtil.getGravityScale(9.8), DELTA);
        assertEquals(5.0 / 9.8, GravityDirectionUtil.getGravityScale(5.0), DELTA);
        assertEquals(15.0 / 9.8, GravityDirectionUtil.getGravityScale(15.0), DELTA);
    }

    @Test
    void changesNaturalAccelerationWithGravityStrength() {
        assertEquals(0.08, GravityDirectionUtil.scaleGravity(0.08, 9.8), DELTA);
        assertEquals(0.08 * 5.0 / 9.8, GravityDirectionUtil.scaleGravity(0.08, 5.0), DELTA);
        assertEquals(0.08 * 15.0 / 9.8, GravityDirectionUtil.scaleGravity(0.08, 15.0), DELTA);
    }

    @Test
    void createsGravityVectorForEveryDirection() {
        assertVector(Direction.DOWN, 0.0, -0.08, 0.0);
        assertVector(Direction.UP, 0.0, 0.08, 0.0);
        assertVector(Direction.NORTH, 0.0, 0.0, -0.08);
        assertVector(Direction.SOUTH, 0.0, 0.0, 0.08);
        assertVector(Direction.WEST, -0.08, 0.0, 0.0);
        assertVector(Direction.EAST, 0.08, 0.0, 0.0);
    }

    @Test
    void appliesSlowFallingOnlyWhileFallingAlongLocalVerticalAxis() {
        assertEquals(0.01, GravityDirectionUtil.getEffectiveGravity(0.08, -0.2, true), DELTA);
        assertEquals(0.08, GravityDirectionUtil.getEffectiveGravity(0.08, 0.2, true), DELTA);
        assertEquals(0.08, GravityDirectionUtil.getEffectiveGravity(0.08, -0.2, false), DELTA);
        assertEquals(0.0, GravityDirectionUtil.getEffectiveGravity(0.0, -0.2, true), DELTA);
    }

    @Test
    void fallingAlongEveryGravityDirectionIsNotUpwardMovement() {
        for (Direction direction : Direction.values()) {
            Vec3 fallingMovement = GravityDirectionUtil.getGravityVector(direction, 0.08, 9.8);
            assertFalse(GravityDirectionUtil.isMovingAgainstGravity(fallingMovement, direction), direction.getName());
        }
    }

    @Test
    void movementOppositeEveryGravityDirectionIsUpwardMovement() {
        for (Direction direction : Direction.values()) {
            Vec3 upwardMovement = GravityDirectionUtil.getGravityVector(direction, 0.08, 9.8).reverse();
            assertTrue(GravityDirectionUtil.isMovingAgainstGravity(upwardMovement, direction), direction.getName());
        }
    }

    private static void assertVector(final Direction direction, final double x, final double y, final double z) {
        Vec3 vector = GravityDirectionUtil.getGravityVector(direction, 0.08, 9.8);
        assertEquals(x, vector.x, DELTA);
        assertEquals(y, vector.y, DELTA);
        assertEquals(z, vector.z, DELTA);
    }
}
