package com.moigferdsrte.gravitychanger.client;

import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GravityCoreRotationAnimationTest {
    private static final double DELTA = 1.0E-5D;

    @Test
    void everyCoreTransitionRotatesGravityToTargetAroundAnEdge() {
        for (Direction previous : Direction.values()) {
            for (Direction target : Direction.values()) {
                if (previous.getAxis() == target.getAxis()) {
                    continue;
                }

                GravityCoreRotationAnimation animation = new GravityCoreRotationAnimation();
                animation.start(previous, target, 0L);
                Quaternionf rotation = animation.getRotation(GravityCoreRotationAnimation.DURATION_NANOS);
                Vec3 gravity = RotationUtil.vecPlayerToWorld(new Vec3(0.0, -1.0, 0.0), rotation);

                assertEquals(target.getStepX(), gravity.x, DELTA, previous + " -> " + target);
                assertEquals(target.getStepY(), gravity.y, DELTA, previous + " -> " + target);
                assertEquals(target.getStepZ(), gravity.z, DELTA, previous + " -> " + target);
            }
        }
    }

    @Test
    void everyCoreTransitionEndsAtTheStandardTargetRotation() {
        for (Direction previous : Direction.values()) {
            for (Direction target : Direction.values()) {
                if (previous.getAxis() == target.getAxis()) {
                    continue;
                }

                GravityCoreRotationAnimation animation = new GravityCoreRotationAnimation();
                animation.start(previous, target, 0L);
                Quaternionf actual = animation.getRotation(GravityCoreRotationAnimation.DURATION_NANOS);
                Quaternionf expected = RotationUtil.getEntityRotationQuaternion(target);

                assertTrue(actual.equals(expected, (float)DELTA), previous + " -> " + target);
            }
        }
    }

}
