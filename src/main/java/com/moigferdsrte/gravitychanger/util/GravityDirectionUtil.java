package com.moigferdsrte.gravitychanger.util;

import com.moigferdsrte.gravitychanger.attributes.DirectionalAttribute;
import com.moigferdsrte.gravitychanger.init.ModAttributes;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

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

        attribute.setBaseValue(DirectionalAttribute.valueOf(direction));
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

        if (previousDirection == Direction.DOWN && direction != Direction.DOWN) {
            entity.teleportTo(entity.getX(), entity.getY() + DOWN_TO_NON_DOWN_LIFT, entity.getZ());
        }

        return true;
    }

    public static double getGravityStrength(final Entity entity) {
        if (entity instanceof LivingEntity living) {
            AttributeMap attributes = living.getAttributes();
            if (attributes != null && attributes.hasAttribute(ModAttributes.GRAVITY_STRENGTH)) {
                return living.getAttributeValue(ModAttributes.GRAVITY_STRENGTH);
            }
        }

        return DEFAULT_GRAVITY_STRENGTH;
    }

    public static double getGravityScale(final Entity entity) {
        return getGravityStrength(entity) / DEFAULT_GRAVITY_STRENGTH;
    }

    public static double scaleGravity(final Entity entity, final double gravity) {
        return gravity * getGravityScale(entity);
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
        Direction direction = getGravityDirection(entity);
        double scaledGravity = scaleGravity(entity, gravity);
        return new Vec3(
            direction.getStepX() * scaledGravity,
            direction.getStepY() * scaledGravity,
            direction.getStepZ() * scaledGravity
        );
    }

    public static Vec3 applyGravity(final Entity entity, final Vec3 movement, final double gravity) {
        Direction direction = getGravityDirection(entity);
        if (direction == Direction.DOWN) {
            return movement;
        }

        return movement.add(getGravityVector(entity, gravity));
    }
}
