package com.moigferdsrte.gravitychanger.util;

import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class RotationUtil {
    private static final Quaternionf[] WORLD_ROTATION_QUATERNIONS = new Quaternionf[Direction.values().length];
    private static final Quaternionf[] ENTITY_ROTATION_QUATERNIONS = new Quaternionf[Direction.values().length];

    static {
        WORLD_ROTATION_QUATERNIONS[Direction.DOWN.get3DDataValue()] = new Quaternionf();
        WORLD_ROTATION_QUATERNIONS[Direction.UP.get3DDataValue()] = Axis.ZP.rotationDegrees(-180.0F);
        WORLD_ROTATION_QUATERNIONS[Direction.NORTH.get3DDataValue()] = Axis.XP.rotationDegrees(-90.0F);
        WORLD_ROTATION_QUATERNIONS[Direction.SOUTH.get3DDataValue()] = Axis.XP.rotationDegrees(-90.0F).mul(Axis.YP.rotationDegrees(-180.0F));
        WORLD_ROTATION_QUATERNIONS[Direction.WEST.get3DDataValue()] = Axis.XP.rotationDegrees(-90.0F).mul(Axis.YP.rotationDegrees(-90.0F));
        WORLD_ROTATION_QUATERNIONS[Direction.EAST.get3DDataValue()] = Axis.XP.rotationDegrees(-90.0F).mul(Axis.YP.rotationDegrees(-270.0F));

        for (Direction direction : Direction.values()) {
            ENTITY_ROTATION_QUATERNIONS[direction.get3DDataValue()] = new Quaternionf(WORLD_ROTATION_QUATERNIONS[direction.get3DDataValue()]).conjugate();
        }
    }

    private RotationUtil() {
    }

    public static Vec3 vecWorldToPlayer(final Vec3 vec, final Direction gravityDirection) {
        return vecWorldToPlayer(vec.x, vec.y, vec.z, gravityDirection);
    }

    public static Vec3 vecWorldToPlayer(final Vec3 vec, final Quaternionf entityRotation) {
        Vector3f transformed = new Quaternionf(entityRotation)
            .conjugate()
            .transform(new Vector3f((float)vec.x, (float)vec.y, (float)vec.z));
        return new Vec3(transformed.x, transformed.y, transformed.z);
    }

    public static Vec3 vecWorldToPlayer(final double x, final double y, final double z, final Direction gravityDirection) {
        return switch (gravityDirection) {
            case DOWN -> new Vec3(x, y, z);
            case UP -> new Vec3(-x, -y, z);
            case NORTH -> new Vec3(x, z, -y);
            case SOUTH -> new Vec3(-x, -z, -y);
            case WEST -> new Vec3(-z, x, -y);
            case EAST -> new Vec3(z, -x, -y);
        };
    }

    public static Vec3 vecPlayerToWorld(final Vec3 vec, final Direction gravityDirection) {
        return vecPlayerToWorld(vec.x, vec.y, vec.z, gravityDirection);
    }

    public static Vec3 vecPlayerToWorld(final Vec3 vec, final Quaternionf entityRotation) {
        Vector3f transformed = new Quaternionf(entityRotation)
            .transform(new Vector3f((float)vec.x, (float)vec.y, (float)vec.z));
        return new Vec3(transformed.x, transformed.y, transformed.z);
    }

    public static Vec3 vecPlayerToWorld(final double x, final double y, final double z, final Direction gravityDirection) {
        return switch (gravityDirection) {
            case DOWN -> new Vec3(x, y, z);
            case UP -> new Vec3(-x, -y, z);
            case NORTH -> new Vec3(x, -z, y);
            case SOUTH -> new Vec3(-x, -z, -y);
            case WEST -> new Vec3(y, -z, -x);
            case EAST -> new Vec3(-y, -z, x);
        };
    }

    public static Vec3 maskPlayerToWorld(final double x, final double y, final double z, final Direction gravityDirection) {
        return switch (gravityDirection) {
            case DOWN, UP -> new Vec3(x, y, z);
            case NORTH, SOUTH -> new Vec3(x, z, y);
            case WEST, EAST -> new Vec3(y, z, x);
        };
    }

    public static AABB boxPlayerToWorld(final AABB box, final Direction gravityDirection) {
        return new AABB(
            vecPlayerToWorld(box.minX, box.minY, box.minZ, gravityDirection),
            vecPlayerToWorld(box.maxX, box.maxY, box.maxZ, gravityDirection)
        );
    }

    public static AABB boxWorldToPlayer(final AABB box, final Direction gravityDirection) {
        return new AABB(
            vecWorldToPlayer(box.minX, box.minY, box.minZ, gravityDirection),
            vecWorldToPlayer(box.maxX, box.maxY, box.maxZ, gravityDirection)
        );
    }

    public static AABB makeBoxFromDimensions(final EntityDimensions dimensions, final Direction gravityDirection, final Vec3 pos) {
        if (gravityDirection == Direction.DOWN) {
            return dimensions.makeBoundingBox(pos);
        }

        float halfWidth = dimensions.width() / 2.0F;
        AABB localBox = new AABB(-halfWidth, 0.0, -halfWidth, halfWidth, dimensions.height(), halfWidth);
        return boxPlayerToWorld(localBox, gravityDirection).move(pos);
    }

    public static AABB makeDirectionalEyeBox(final Vec3 eyePosition, final float width, final Direction gravityDirection) {
        AABB localEyeBox = AABB.ofSize(Vec3.ZERO, width, 1.0E-6, width);
        return boxPlayerToWorld(localEyeBox, gravityDirection).move(eyePosition);
    }

    public static Vec3 getCenterAlignedPosition(
        final AABB previousBoundingBox,
        final EntityDimensions dimensions,
        final Direction gravityDirection
    ) {
        Vec3 relativeCenter = makeBoxFromDimensions(dimensions, gravityDirection, Vec3.ZERO).getCenter();
        return previousBoundingBox.getCenter().subtract(relativeCenter);
    }

    public static Vec2 rotWorldToPlayer(final float yaw, final float pitch, final Direction gravityDirection) {
        return vecToRot(vecWorldToPlayer(rotToVec(yaw, pitch), gravityDirection));
    }

    public static Vec2 rotPlayerToWorld(final float yaw, final float pitch, final Direction gravityDirection) {
        return vecToRot(vecPlayerToWorld(rotToVec(yaw, pitch), gravityDirection));
    }

    public static Vec3 rotToVec(final float yaw, final float pitch) {
        double radPitch = pitch * (Math.PI / 180.0);
        double radNegYaw = -yaw * (Math.PI / 180.0);
        double cosNegYaw = Math.cos(radNegYaw);
        double sinNegYaw = Math.sin(radNegYaw);
        double cosPitch = Math.cos(radPitch);
        double sinPitch = Math.sin(radPitch);
        return new Vec3(sinNegYaw * cosPitch, -sinPitch, cosNegYaw * cosPitch);
    }

    public static Vec2 vecToRot(final Vec3 vec) {
        double horizontalLength = Math.sqrt(vec.x * vec.x + vec.z * vec.z);
        float yaw = (float)(Math.atan2(vec.z, vec.x) * (180.0 / Math.PI)) - 90.0F;
        float pitch = (float)(-(Math.atan2(vec.y, horizontalLength) * (180.0 / Math.PI)));
        return new Vec2(yaw, pitch);
    }

    public static Quaternionf getEntityRotationQuaternion(final Direction gravityDirection) {
        return new Quaternionf(ENTITY_ROTATION_QUATERNIONS[gravityDirection.get3DDataValue()]);
    }

    public static Quaternionf getCameraRotationQuaternion(final Direction gravityDirection) {
        return new Quaternionf(ENTITY_ROTATION_QUATERNIONS[gravityDirection.get3DDataValue()]);
    }

    public static Quaternionf getWorldRotationQuaternion(final Direction gravityDirection) {
        return new Quaternionf(WORLD_ROTATION_QUATERNIONS[gravityDirection.get3DDataValue()]);
    }

    public static boolean isIdentityRotation(final Quaternionf rotation) {
        double epsilon = 1.0E-5;
        return Math.abs(rotation.x()) < epsilon
            && Math.abs(rotation.y()) < epsilon
            && Math.abs(rotation.z()) < epsilon
            && Math.abs(Math.abs(rotation.w()) - 1.0F) < epsilon;
    }

    public static Vector3f vecPlayerToWorld(final float x, final float y, final float z, final Direction gravityDirection) {
        Vec3 result = vecPlayerToWorld((double)x, (double)y, (double)z, gravityDirection);
        return new Vector3f((float)result.x, (float)result.y, (float)result.z);
    }
}
