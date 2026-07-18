package com.moigferdsrte.gravitychanger.client;

import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GravityRotationAnimationTest {
    private static final float DELTA = 1.0E-5F;

    @Test
    void initialDirectionUsesItsFinalRotationImmediately() {
        GravityRotationAnimation animation = new GravityRotationAnimation();

        Quaternionf rotation = animation.getRotation(Direction.NORTH, 100L);

        assertEquivalent(RotationUtil.getEntityRotationQuaternion(Direction.NORTH), rotation);
    }

    @Test
    void directionChangeInterpolatesAndFinishesAtTargetRotation() {
        GravityRotationAnimation animation = new GravityRotationAnimation();
        long startTime = 100L;
        animation.getRotation(Direction.DOWN, startTime);
        animation.getRotation(Direction.NORTH, startTime);

        Quaternionf halfway = animation.getRotation(
            Direction.NORTH,
            startTime + GravityRotationAnimation.DURATION_NANOS / 2L
        );
        Quaternionf finished = animation.getRotation(
            Direction.NORTH,
            startTime + GravityRotationAnimation.DURATION_NANOS
        );

        assertFalse(equivalent(RotationUtil.getEntityRotationQuaternion(Direction.DOWN), halfway));
        assertFalse(equivalent(RotationUtil.getEntityRotationQuaternion(Direction.NORTH), halfway));
        assertEquivalent(RotationUtil.getEntityRotationQuaternion(Direction.NORTH), finished);
    }

    @Test
    void interruptedTransitionContinuesFromCurrentVisualRotation() {
        GravityRotationAnimation animation = new GravityRotationAnimation();
        long startTime = 100L;
        animation.getRotation(Direction.DOWN, startTime);
        animation.getRotation(Direction.NORTH, startTime);
        long interruptionTime = startTime + GravityRotationAnimation.DURATION_NANOS / 2L;
        Quaternionf beforeInterruption = animation.getRotation(Direction.NORTH, interruptionTime);

        Quaternionf afterInterruption = animation.getRotation(Direction.EAST, interruptionTime);

        assertEquivalent(beforeInterruption, afterInterruption);
    }

    private static void assertEquivalent(final Quaternionf expected, final Quaternionf actual) {
        assertEquals(1.0F, Math.abs(expected.dot(actual)), DELTA);
    }

    private static boolean equivalent(final Quaternionf first, final Quaternionf second) {
        return Math.abs(Math.abs(first.dot(second)) - 1.0F) < DELTA;
    }
}
