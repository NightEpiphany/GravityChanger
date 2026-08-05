package com.moigferdsrte.gravitychanger.util;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class ElytraFlightUtil {
    private static final double FIREWORK_TARGET_SPEED = 1.5;
    private static final double FIREWORK_ACCELERATION = 0.1;
    private static final double FIREWORK_VELOCITY_BLEND = 0.5;

    private ElytraFlightUtil() {
    }

    public static Vec3 updateMovement(
        final Vec3 worldMovement,
        final Vec3 worldLook,
        final float leanAngle,
        final double gravity,
        final Direction gravityDirection
    ) {
        Vec3 movement = RotationUtil.vecWorldToPlayer(worldMovement, gravityDirection);
        Vec3 look = RotationUtil.vecWorldToPlayer(worldLook, gravityDirection);
        double lookHorizontalLength = Math.sqrt(look.x * look.x + look.z * look.z);
        double movementHorizontalLength = movement.horizontalDistance();
        double liftForce = Mth.square(Math.cos(leanAngle));

        movement = movement.add(0.0, gravity * (-1.0 + liftForce * 0.75), 0.0);
        if (movement.y < 0.0 && lookHorizontalLength > 0.0) {
            double conversion = movement.y * -0.1 * liftForce;
            movement = movement.add(
                look.x * conversion / lookHorizontalLength,
                conversion,
                look.z * conversion / lookHorizontalLength
            );
        }

        if (leanAngle < 0.0F && lookHorizontalLength > 0.0) {
            double conversion = movementHorizontalLength * -Mth.sin(leanAngle) * 0.04;
            movement = movement.add(
                -look.x * conversion / lookHorizontalLength,
                conversion * 3.2,
                -look.z * conversion / lookHorizontalLength
            );
        }

        if (lookHorizontalLength > 0.0) {
            movement = movement.add(
                (look.x / lookHorizontalLength * movementHorizontalLength - movement.x) * 0.1,
                0.0,
                (look.z / lookHorizontalLength * movementHorizontalLength - movement.z) * 0.1
            );
        }

        return RotationUtil.vecPlayerToWorld(
            movement.multiply(0.99F, 0.98F, 0.99F),
            gravityDirection
        );
    }

    public static double horizontalDistance(final Vec3 worldMovement, final Direction gravityDirection) {
        return RotationUtil.vecWorldToPlayer(worldMovement, gravityDirection).horizontalDistance();
    }

    public static Vec3 applyFireworkBoost(
        final Vec3 worldMovement,
        final Vec3 worldLook,
        final Direction gravityDirection
    ) {
        Vec3 movement = RotationUtil.vecWorldToPlayer(worldMovement, gravityDirection);
        Vec3 look = RotationUtil.vecWorldToPlayer(worldLook, gravityDirection);
        Vec3 boostedMovement = movement.add(
            look.x * FIREWORK_ACCELERATION + (look.x * FIREWORK_TARGET_SPEED - movement.x) * FIREWORK_VELOCITY_BLEND,
            look.y * FIREWORK_ACCELERATION + (look.y * FIREWORK_TARGET_SPEED - movement.y) * FIREWORK_VELOCITY_BLEND,
            look.z * FIREWORK_ACCELERATION + (look.z * FIREWORK_TARGET_SPEED - movement.z) * FIREWORK_VELOCITY_BLEND
        );
        return RotationUtil.vecPlayerToWorld(boostedMovement, gravityDirection);
    }
}
