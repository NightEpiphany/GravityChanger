package com.moigferdsrte.gravitychanger.entity;

import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class DirectionalKnockbackUtil {
    private static final double MINIMUM_DIRECTION_LENGTH_SQR = 1.0E-5;

    private DirectionalKnockbackUtil() {
    }

    public static Vec3 getDirectionTowardAttacker(
        final Vec3 targetPosition,
        final Vec3 attackerPosition,
        final Direction targetGravity
    ) {
        Vec3 localDelta = RotationUtil.vecWorldToPlayer(
            attackerPosition.subtract(targetPosition),
            targetGravity
        );
        Vec3 localHorizontal = new Vec3(localDelta.x, 0.0, localDelta.z);
        if (localHorizontal.lengthSqr() < MINIMUM_DIRECTION_LENGTH_SQR) {
            return Vec3.ZERO;
        }

        return RotationUtil.vecPlayerToWorld(localHorizontal.normalize(), targetGravity);
    }

    public static Vec3 applyKnockback(
        final Vec3 movement,
        final Vec3 directionTowardAttacker,
        final double strength,
        final boolean onGround,
        final Direction targetGravity
    ) {
        Vec3 localMovement = RotationUtil.vecWorldToPlayer(movement, targetGravity);
        Vec3 localDirection = RotationUtil.vecWorldToPlayer(directionTowardAttacker, targetGravity).normalize();
        double localY = onGround
            ? Math.min(0.4, localMovement.y / 2.0 + strength)
            : localMovement.y;
        return RotationUtil.vecPlayerToWorld(
            localMovement.x / 2.0 - localDirection.x * strength,
            localY,
            localMovement.z / 2.0 - localDirection.z * strength,
            targetGravity
        );
    }
}
