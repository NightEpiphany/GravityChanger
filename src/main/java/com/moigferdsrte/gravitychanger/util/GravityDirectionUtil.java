package com.moigferdsrte.gravitychanger.util;

import com.moigferdsrte.gravitychanger.attributes.DirectionalAttribute;
import com.moigferdsrte.gravitychanger.init.ModAttributes;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
public final class GravityDirectionUtil {
    public static final double DEFAULT_GRAVITY_STRENGTH = 9.8;
    public static final double MIN_GRAVITY_STRENGTH = 5.0;
    public static final double MAX_GRAVITY_STRENGTH = 15.0;
    public static final double DOWN_TO_NON_DOWN_LIFT = 2.0;

    private GravityDirectionUtil() {
    }

    @SuppressWarnings("all")
    public static Direction getGravityDirection(final Entity entity) {
        if (entity instanceof Player && entity.isPassenger()) {
            Entity vehicle = entity.getVehicle();
            if (vehicle != null) {
                return getGravityDirection(vehicle);
            }
        }

        return getOwnGravityDirection(entity);
    }

    @SuppressWarnings("all")
    public static Direction getOwnGravityDirection(final Entity entity) {
        if (entity instanceof LivingEntity living) {
            AttributeMap attributes = living.getAttributes();
            if (attributes != null && attributes.hasAttribute(ModAttributes.GRAVITY_DIRECTION)) {
                return DirectionalAttribute.toDirection(living.getAttributeValue(ModAttributes.GRAVITY_DIRECTION));
            }
        }

        return Direction.DOWN;
    }

    public static boolean setGravityDirection(final LivingEntity entity, final Direction direction) {
        AttributeInstance attribute = entity.getAttribute(ModAttributes.GRAVITY_DIRECTION);
        if (attribute == null) {
            return false;
        }

        Direction previousDirection = getOwnGravityDirection(entity);
        if (previousDirection == direction) {
            return false;
        }

        AABB previousBoundingBox = entity.getBoundingBox();
        attribute.setBaseValue(DirectionalAttribute.valueOf(direction));
        if (!entity.level().isClientSide() && !(entity instanceof Player)) {
            Vec3 alignedPosition = RotationUtil.getCenterAlignedPosition(
                previousBoundingBox,
                entity.getDimensions(entity.getPose()),
                direction
            );
            entity.teleportTo(alignedPosition.x, alignedPosition.y, alignedPosition.z);
        }
        return true;
    }

    public static boolean setGravityDirectionWithDownLift(final LivingEntity entity, final Direction direction) {
        Direction previousDirection = getGravityDirection(entity);
        if (previousDirection == direction) {
            return false;
        }

        if (!setGravityDirection(entity, direction)) {
            return false;
        }

        if (entity instanceof Player && previousDirection == Direction.DOWN && direction != Direction.DOWN) {
            entity.teleportTo(entity.getX(), entity.getY() + DOWN_TO_NON_DOWN_LIFT, entity.getZ());
        }

        return true;
    }

    @SuppressWarnings("all")
    public static double getGravityStrength(final Entity entity) {
        if (entity instanceof LivingEntity living) {
            AttributeMap attributes = living.getAttributes();
            if (attributes != null && attributes.hasAttribute(ModAttributes.GRAVITY_STRENGTH)) {
                return living.getAttributeValue(ModAttributes.GRAVITY_STRENGTH);
            }
        }

        return DEFAULT_GRAVITY_STRENGTH;
    }

    @SuppressWarnings("all")
    public static void setGravityStrength(final Entity entity, final double gravityStrength) {
        if (entity instanceof LivingEntity living) {
            AttributeMap attributes = living.getAttributes();
            if (attributes != null && attributes.hasAttribute(ModAttributes.GRAVITY_STRENGTH)) {
                attributes.getInstance(ModAttributes.GRAVITY_STRENGTH).setBaseValue(gravityStrength);
            }
        }
    }

    public static double getGravityScale(final Entity entity) {
        return getGravityScale(getGravityStrength(entity));
    }

    public static double getGravityScale(final double gravityStrength) {
        return gravityStrength / DEFAULT_GRAVITY_STRENGTH;
    }

    public static double scaleGravity(final Entity entity, final double gravity) {
        return scaleGravity(gravity, getGravityStrength(entity));
    }

    public static double scaleGravity(final double gravity, final double gravityStrength) {
        return gravity * getGravityScale(gravityStrength);
    }

    public static double getEffectiveGravity(
        final double gravity,
        final double localVerticalVelocity,
        final boolean hasSlowFalling
    ) {
        return localVerticalVelocity <= 0.0 && hasSlowFalling
            ? Math.min(gravity, 0.01)
            : gravity;
    }

    public static double getJumpVelocityScale(final Entity entity) {
        return Math.sqrt(DEFAULT_GRAVITY_STRENGTH / getGravityStrength(entity));
    }

    public static float getMovementSpeedScale(final Entity entity) {
        double strength = getGravityStrength(entity);
        if (strength <= DEFAULT_GRAVITY_STRENGTH) {
            return 1.0F;
        }

        double highRangeProgress = (strength - DEFAULT_GRAVITY_STRENGTH) / (MAX_GRAVITY_STRENGTH - DEFAULT_GRAVITY_STRENGTH);
        return (float)(1.0 - highRangeProgress * 0.28);
    }

    public static double getFallDistanceScale(final Entity entity) {
        double strength = getGravityStrength(entity);
        if (strength <= DEFAULT_GRAVITY_STRENGTH) {
            return strength / DEFAULT_GRAVITY_STRENGTH;
        }

        double highRangeProgress = (strength - DEFAULT_GRAVITY_STRENGTH) / (MAX_GRAVITY_STRENGTH - DEFAULT_GRAVITY_STRENGTH);
        return 1.0 + highRangeProgress * 5.0;
    }

    public static Vec3 getGravityVector(final Entity entity, final double gravity) {
        return getGravityVector(getGravityDirection(entity), gravity, getGravityStrength(entity));
    }

    public static Vec3 getGravityVector(
        final Direction direction,
        final double gravity,
        final double gravityStrength
    ) {
        double scaledGravity = scaleGravity(gravity, gravityStrength);
        return new Vec3(
            direction.getStepX() * scaledGravity,
            direction.getStepY() * scaledGravity,
            direction.getStepZ() * scaledGravity
        );
    }

    public static boolean isMovingAgainstGravity(final Vec3 worldMovement, final Direction gravityDirection) {
        return RotationUtil.vecWorldToPlayer(worldMovement, gravityDirection).y > 0.0;
    }

    public static Vec3 applyGravity(final Entity entity, final Vec3 movement, final double gravity) {
        Direction direction = getGravityDirection(entity);
        if (direction == Direction.DOWN) {
            return movement;
        }

        return movement.add(getGravityVector(entity, gravity));
    }
}
