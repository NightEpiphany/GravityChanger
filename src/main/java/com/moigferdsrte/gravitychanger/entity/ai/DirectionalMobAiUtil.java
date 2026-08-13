package com.moigferdsrte.gravitychanger.entity.ai;

import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class DirectionalMobAiUtil {
    private static final double MINIMUM_VECTOR_LENGTH_SQR = 1.0E-12;

    private DirectionalMobAiUtil() {
    }

    public static Vec3 projectOntoMovementPlane(
        final Vec3 origin,
        final Vec3 target,
        final Direction gravityDirection
    ) {
        Vec3 localDelta = RotationUtil.vecWorldToPlayer(target.subtract(origin), gravityDirection);
        return origin.add(RotationUtil.vecPlayerToWorld(localDelta.x, 0.0, localDelta.z, gravityDirection));
    }

    public static Vec3 getAttackBoxInflation(final double range, final Direction gravityDirection) {
        return RotationUtil.maskPlayerToWorld(range, 0.0, range, gravityDirection);
    }

    public static Vec3 getRangedLaunchPosition(
        final Vec3 eyePosition,
        final Vec3 vanillaHorizontalOffset,
        final Direction shooterGravity
    ) {
        Vec3 localOffset = vanillaHorizontalOffset.add(0.0, -0.1, 0.0);
        return eyePosition.add(RotationUtil.vecPlayerToWorld(localOffset, shooterGravity));
    }

    public static Vec3 getRangedTargetPosition(
        final Vec3 targetPosition,
        final double targetHeight,
        final Direction targetGravity
    ) {
        return targetPosition.add(RotationUtil.vecPlayerToWorld(0.0, targetHeight / 3.0, 0.0, targetGravity));
    }

    public static Vec3 getBallisticDirection(
        final Vec3 launchPosition,
        final Vec3 targetPosition,
        final Direction shooterGravity,
        final double arcFactor
    ) {
        Vec3 targetDelta = targetPosition.subtract(launchPosition);
        Vec3 localDelta = RotationUtil.vecWorldToPlayer(targetDelta, shooterGravity);
        double horizontalDistance = Math.sqrt(localDelta.x * localDelta.x + localDelta.z * localDelta.z);
        return targetDelta.add(RotationUtil.vecPlayerToWorld(0.0, horizontalDistance * arcFactor, 0.0, shooterGravity));
    }

    public static Vec3 rotateBetweenReferences(
        final Vec3 vector,
        final Vec3 fromReference,
        final Vec3 toReference
    ) {
        if (vector.lengthSqr() < MINIMUM_VECTOR_LENGTH_SQR
            || fromReference.lengthSqr() < MINIMUM_VECTOR_LENGTH_SQR
            || toReference.lengthSqr() < MINIMUM_VECTOR_LENGTH_SQR) {
            return toReference;
        }

        Vec3 from = fromReference.normalize();
        Vec3 to = toReference.normalize();
        double cosine = Math.clamp(from.dot(to), -1.0, 1.0);
        if (cosine > 1.0 - 1.0E-8) {
            return vector;
        }

        if (cosine < -1.0 + 1.0E-8) {
            Vec3 axis = Math.abs(from.x) < 0.9
                ? from.cross(new Vec3(1.0, 0.0, 0.0)).normalize()
                : from.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
            return axis.scale(2.0 * axis.dot(vector)).subtract(vector);
        }

        Vec3 axis = from.cross(to);
        double sine = axis.length();
        Vec3 unitAxis = axis.scale(1.0 / sine);
        return vector.scale(cosine)
            .add(unitAxis.cross(vector).scale(sine))
            .add(unitAxis.scale(unitAxis.dot(vector) * (1.0 - cosine)));
    }
}
