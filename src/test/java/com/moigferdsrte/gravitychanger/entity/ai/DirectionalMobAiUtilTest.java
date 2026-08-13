package com.moigferdsrte.gravitychanger.entity.ai;

import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionalMobAiUtilTest {
    private static final double DELTA = 1.0E-5;

    @Test
    void projectedTargetHasNoGravityAxisOffset() {
        Vec3 origin = new Vec3(2.25, 7.5, -4.75);
        Vec3 target = new Vec3(-3.0, 12.0, 8.0);

        for (Direction gravity : Direction.values()) {
            Vec3 projected = DirectionalMobAiUtil.projectOntoMovementPlane(origin, target, gravity);
            Vec3 delta = projected.subtract(origin);
            double gravityAxisOffset = delta.x * gravity.getStepX()
                + delta.y * gravity.getStepY()
                + delta.z * gravity.getStepZ();
            assertEquals(0.0, gravityAxisOffset, DELTA, gravity.toString());
        }
    }

    @Test
    void attackBoxExpandsOnlyAcrossTheLocalHorizontalPlane() {
        double range = 1.25;

        for (Direction gravity : Direction.values()) {
            Vec3 inflation = DirectionalMobAiUtil.getAttackBoxInflation(range, gravity);
            assertEquals(gravity.getAxis() == Direction.Axis.X ? 0.0 : range, inflation.x, DELTA, gravity + " x");
            assertEquals(gravity.getAxis() == Direction.Axis.Y ? 0.0 : range, inflation.y, DELTA, gravity + " y");
            assertEquals(gravity.getAxis() == Direction.Axis.Z ? 0.0 : range, inflation.z, DELTA, gravity + " z");
        }
    }

    @Test
    void ballisticLiftAlwaysPointsAgainstGravity() {
        Vec3 launch = new Vec3(2.0, 3.0, 5.0);

        for (Direction gravity : Direction.values()) {
            Vec3 localTargetDelta = new Vec3(6.0, 1.5, -8.0);
            Vec3 target = launch.add(RotationUtil.vecPlayerToWorld(localTargetDelta, gravity));
            Vec3 ballistic = DirectionalMobAiUtil.getBallisticDirection(launch, target, gravity, 0.2);
            Vec3 localBallistic = RotationUtil.vecWorldToPlayer(ballistic, gravity);

            assertEquals(localTargetDelta.x, localBallistic.x, DELTA, gravity + " x");
            assertEquals(localTargetDelta.y + 2.0, localBallistic.y, DELTA, gravity + " y");
            assertEquals(localTargetDelta.z, localBallistic.z, DELTA, gravity + " z");
        }
    }

    @Test
    void launchOffsetRotatesWithShooterGravity() {
        Vec3 eyePosition = new Vec3(10.0, 21.5, 30.0);
        Vec3 vanillaHorizontalOffset = new Vec3(0.25, 0.0, -0.35);

        for (Direction gravity : Direction.values()) {
            Vec3 launch = DirectionalMobAiUtil.getRangedLaunchPosition(eyePosition, vanillaHorizontalOffset, gravity);
            Vec3 localOffset = RotationUtil.vecWorldToPlayer(launch.subtract(eyePosition), gravity);

            assertEquals(0.25, localOffset.x, DELTA, gravity + " x");
            assertEquals(-0.1, localOffset.y, DELTA, gravity + " y");
            assertEquals(-0.35, localOffset.z, DELTA, gravity + " z");
        }
    }

    @Test
    void referenceRotationMapsAimAndPreservesLength() {
        Vec3 source = new Vec3(4.0, 1.0, -2.0);
        Vec3 target = new Vec3(-1.0, 3.0, 5.0);
        Vec3 rotated = DirectionalMobAiUtil.rotateBetweenReferences(source, source, target);

        assertEquals(source.length(), rotated.length(), DELTA);
        assertEquals(1.0, rotated.normalize().dot(target.normalize()), DELTA);
    }
}
