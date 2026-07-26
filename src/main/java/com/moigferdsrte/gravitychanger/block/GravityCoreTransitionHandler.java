package com.moigferdsrte.gravitychanger.block;

import com.moigferdsrte.gravitychanger.api.GravityMovementEntity;
import com.moigferdsrte.gravitychanger.init.ModEntityTags;
import com.moigferdsrte.gravitychanger.init.ModAttributes;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

public final class GravityCoreTransitionHandler {
    private static final double MIN_EDGE_SPEED = 0.01;
    private static final double EDGE_LOOKAHEAD_MARGIN = 0.05;
    private static final double SUPPORT_OFFSET = 5.0E-7;
    private static final double COLLISION_EPSILON = 1.0E-7;
    private static final double JUMP_EPSILON = 1.0E-5;

    private GravityCoreTransitionHandler() {
    }

    public static void tryTransitionOnFall(final Level level, final LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)
            || !(entity instanceof GravityMovementEntity movementEntity)
            || entity.isAlive() == false
            || entity.isPassenger()
            || entity.isNoGravity()
            || entity.noPhysics) {
            return;
        }

        BlockPos corePos = movementEntity.gravitychanger$getLastGravityCore();
        if (corePos == null) {
            return;
        }

        if (entity.onGround()) {
            if (!entity.isSupportedBy(corePos)) {
                movementEntity.gravitychanger$clearGravityCore();
            }
            return;
        }

        if (entity.tickCount - movementEntity.gravitychanger$getLastGravityCoreTick() > 1) {
            movementEntity.gravitychanger$clearGravityCore();
            return;
        }

        Direction currentGravity = GravityDirectionUtil.getOwnGravityDirection(entity);
        Vec3 positionMovement = entity.position().subtract(entity.oldPosition());
        Vec3 movement = getTransitionMovement(entity, currentGravity);
        Vec3 localMovement = RotationUtil.vecWorldToPlayer(movement, currentGravity);
        Vec3 localPositionMovement = RotationUtil.vecWorldToPlayer(positionMovement, currentGravity);
        if (localMovement.y >= -JUMP_EPSILON && localPositionMovement.y >= -JUMP_EPSILON) {
            return;
        }
        if (localMovement.y > JUMP_EPSILON || localPositionMovement.y > JUMP_EPSILON) {
            movementEntity.gravitychanger$clearGravityCore();
            return;
        }

        Direction edgeDirection = findFallTransitionEdge(
            entity.position(),
            entity.oldPosition(),
            movement,
            corePos,
            currentGravity
        );
        if (edgeDirection == null) {
            return;
        }

        Direction targetGravity = edgeDirection.getOpposite();
        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
        Vec3 faceCenter = getFaceCenterPosition(dimensions, corePos, edgeDirection, currentGravity);
        Vec3 targetPosition = findCollisionFreeTargetPosition(
            serverLevel,
            entity,
            faceCenter,
            corePos,
            edgeDirection,
            currentGravity,
            targetGravity
        );
        if (targetPosition == null) {
            return;
        }

        Vec3 targetMovement = rotateAroundEdge(movement, currentGravity, edgeDirection);
        Vec3 targetLook = rotateAroundEdge(entity.getViewVector(1.0F), currentGravity, edgeDirection);
        Vec2 targetRotation = RotationUtil.vecToRot(RotationUtil.vecWorldToPlayer(targetLook, targetGravity));
        float targetBodyRotation = rotateLocalYaw(entity.yBodyRot, currentGravity, edgeDirection, targetGravity);
        float targetHeadRotation = rotateLocalYaw(entity.getYHeadRot(), currentGravity, edgeDirection, targetGravity);

        if (!GravityDirectionUtil.setGravityDirection(entity, targetGravity)) {
            return;
        }

        syncGravityDirectionBeforeTeleport(entity);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.METAL_FALL, entity.getSoundSource(), 1.0F, 0.5F);
        moveEntity(entity, targetPosition, targetMovement, targetRotation);
        entity.setYBodyRot(targetBodyRotation);
        entity.setYHeadRot(targetHeadRotation);
        entity.setOnGround(true);
        entity.resetFallDistance();
        movementEntity.gravitychanger$clearGravityCore();
    }

    static void tryTransition(final Level level, final BlockPos corePos, final LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)
            || !entity.isAlive()
            || entity.isPassenger()
            || entity.isNoGravity()
            || entity.noPhysics
            || !entity.onGround()
            || !entity.isSupportedBy(corePos)
            || entity.getType().builtInRegistryHolder().is(ModEntityTags.GRAVITY_FIXED)) {
            return;
        }

        Direction currentGravity = GravityDirectionUtil.getOwnGravityDirection(entity);
        Vec3 movement = getTransitionMovement(entity, currentGravity);
        Vec3 localMovement = RotationUtil.vecWorldToPlayer(movement, currentGravity);
        Vec3 localPositionMovement = RotationUtil.vecWorldToPlayer(
            entity.position().subtract(entity.oldPosition()),
            currentGravity
        );
        if (localMovement.y > JUMP_EPSILON || localPositionMovement.y > JUMP_EPSILON) {
            return;
        }

        Direction edgeDirection = findTransitionEdge(entity.position(), movement, corePos, currentGravity);
        if (edgeDirection == null) {
            return;
        }

        Direction targetGravity = edgeDirection.getOpposite();
        Vec3 targetPosition = findCollisionFreeTargetPosition(
            serverLevel,
            entity,
            entity.position(),
            corePos,
            edgeDirection,
            currentGravity,
            targetGravity
        );
        if (targetPosition == null) {
            return;
        }

        Vec3 targetMovement = rotateAroundEdge(movement, currentGravity, edgeDirection);
        Vec3 targetLook = rotateAroundEdge(entity.getViewVector(1.0F), currentGravity, edgeDirection);
        Vec2 targetRotation = RotationUtil.vecToRot(RotationUtil.vecWorldToPlayer(targetLook, targetGravity));
        float targetBodyRotation = rotateLocalYaw(entity.yBodyRot, currentGravity, edgeDirection, targetGravity);
        float targetHeadRotation = rotateLocalYaw(entity.getYHeadRot(), currentGravity, edgeDirection, targetGravity);

        if (!GravityDirectionUtil.setGravityDirection(entity, targetGravity)) {
            return;
        }

        syncGravityDirectionBeforeTeleport(entity);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.METAL_FALL, entity.getSoundSource(), 1.0F, 0.5F);
        moveEntity(entity, targetPosition, targetMovement, targetRotation);
        entity.setYBodyRot(targetBodyRotation);
        entity.setYHeadRot(targetHeadRotation);
        entity.setOnGround(true);
        entity.resetFallDistance();
    }

    private static Vec3 getTransitionMovement(final LivingEntity entity, final Direction gravityDirection) {
        Vec3 collisionMovement = ((GravityMovementEntity)entity).gravitychanger$getLastMoveDelta();
        Vec3 positionMovement = entity.position().subtract(entity.oldPosition());
        Vec3 localCollisionMovement = RotationUtil.vecWorldToPlayer(collisionMovement, gravityDirection);
        Vec3 localPositionMovement = RotationUtil.vecWorldToPlayer(positionMovement, gravityDirection);
        if (localPositionMovement.horizontalDistanceSqr() <= localCollisionMovement.horizontalDistanceSqr()) {
            return collisionMovement;
        }

        return new Vec3(positionMovement.x, collisionMovement.y, positionMovement.z);
    }

    private static void moveEntity(
        final LivingEntity entity,
        final Vec3 position,
        final Vec3 movement,
        final Vec2 rotation
    ) {
        if (entity instanceof ServerPlayer player) {
            player.connection.teleport(
                new PositionMoveRotation(position, movement, rotation.x, rotation.y),
                Set.of()
            );
            return;
        }

        entity.snapTo(position, rotation.x, rotation.y);
        entity.setDeltaMovement(movement);
    }

    /** 在传送旋转包之前同步新重力，避免客户端短暂使用旧重力渲染新视角。 */
    private static void syncGravityDirectionBeforeTeleport(final LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        AttributeInstance gravityDirection = entity.getAttribute(ModAttributes.GRAVITY_DIRECTION);
        if (gravityDirection != null) {
            player.connection.send(new ClientboundUpdateAttributesPacket(
                entity.getId(),
                List.of(gravityDirection)
            ));
        }
    }

    static @Nullable Direction findTransitionEdge(
        final Vec3 position,
        final Vec3 movement,
        final BlockPos corePos,
        final Direction gravityDirection
    ) {
        Direction closestEdge = null;
        double closestArrivalTime = Double.POSITIVE_INFINITY;

        for (Direction edgeDirection : Direction.values()) {
            if (edgeDirection.getAxis() == gravityDirection.getAxis()) {
                continue;
            }

            double speedTowardEdge = component(movement, edgeDirection.getAxis()) * edgeDirection.getAxisDirection().getStep();
            if (speedTowardEdge <= MIN_EDGE_SPEED) {
                continue;
            }

            double boundary = boundary(corePos, edgeDirection);
            double distance = (boundary - component(position, edgeDirection.getAxis()))
                * edgeDirection.getAxisDirection().getStep();
            if (distance > speedTowardEdge + EDGE_LOOKAHEAD_MARGIN) {
                continue;
            }

            double arrivalTime = Math.max(0.0, distance) / speedTowardEdge;
            if (arrivalTime < closestArrivalTime) {
                closestArrivalTime = arrivalTime;
                closestEdge = edgeDirection;
            }
        }

        return closestEdge;
    }

    static Vec3 rotateAroundEdge(
        final Vec3 vector,
        final Direction gravityDirection,
        final Direction edgeDirection
    ) {
        Vec3 gravity = directionVector(gravityDirection);
        Vec3 edge = directionVector(edgeDirection);
        Vec3 targetGravity = edge.reverse();
        double edgeComponent = vector.dot(edge);
        double gravityComponent = vector.dot(gravity);
        Vec3 parallelToEdge = vector
            .subtract(edge.scale(edgeComponent))
            .subtract(gravity.scale(gravityComponent));

        // 绕公共棱边转动 90 度：越过边缘的速度变成沿旧重力方向前进。
        return parallelToEdge
            .add(gravity.scale(edgeComponent))
            .add(targetGravity.scale(gravityComponent));
    }

    static Vec3 getTargetPosition(final Vec3 position, final BlockPos corePos, final Direction edgeDirection) {
        double targetCoordinate = boundary(corePos, edgeDirection)
            + edgeDirection.getAxisDirection().getStep() * SUPPORT_OFFSET;
        return withComponent(position, edgeDirection.getAxis(), targetCoordinate);
    }

    static Vec3 getFaceCenterPosition(
        final EntityDimensions dimensions,
        final BlockPos corePos,
        final Direction edgeDirection,
        final Direction currentGravity
    ) {
        Vec3 center = new Vec3(corePos.getX() + 0.5, corePos.getY() + 0.5, corePos.getZ() + 0.5);
        Vec3 position = center.add(directionVector(currentGravity.getOpposite()).scale(dimensions.width() * 0.5));
        return withComponent(
            position,
            edgeDirection.getAxis(),
            boundary(corePos, edgeDirection) + edgeDirection.getAxisDirection().getStep() * SUPPORT_OFFSET
        );
    }

    private static @Nullable Vec3 findCollisionFreeTargetPosition(
        final ServerLevel level,
        final LivingEntity entity,
        final Vec3 position,
        final BlockPos corePos,
        final Direction edgeDirection,
        final Direction currentGravity,
        final Direction targetGravity
    ) {
        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
        Vec3 basePosition = position;
        Vec3 awayFromSupport = directionVector(currentGravity.getOpposite());
        double maximumShift = dimensions.width() * 0.5 + SUPPORT_OFFSET;
        int steps = Math.max(1, (int)Math.ceil(maximumShift / 0.05));

        for (int step = 0; step <= steps; step++) {
            double shift = step == steps ? maximumShift : step * 0.05;
            Vec3 candidate = basePosition.add(awayFromSupport.scale(shift));
            AABB candidateBox = RotationUtil.makeBoxFromDimensions(dimensions, targetGravity, candidate);
            if (level.noCollision(entity, candidateBox.deflate(COLLISION_EPSILON))) {
                return candidate;
            }
        }

        return null;
    }

    private static @Nullable Direction findFallTransitionEdge(
        final Vec3 position,
        final Vec3 previousPosition,
        final Vec3 movement,
        final BlockPos corePos,
        final Direction gravityDirection
    ) {
        Direction edge = findTransitionEdge(position, movement, corePos, gravityDirection);
        if (edge != null) {
            return edge;
        }

        Vec3 positionMovement = position.subtract(previousPosition);
        double closestArrivalTime = Double.POSITIVE_INFINITY;
        double greatestOutsideDistance = 0.01;
        boolean hasOutsideEdge = false;
        Direction closestEdge = null;
        for (Direction candidate : Direction.values()) {
            if (candidate.getAxis() == gravityDirection.getAxis()) {
                continue;
            }

            double speed = component(positionMovement, candidate.getAxis()) * candidate.getAxisDirection().getStep();
            double distance = (boundary(corePos, candidate) - component(position, candidate.getAxis()))
                * candidate.getAxisDirection().getStep();
            double outsideDistance = -distance;
            if (outsideDistance > greatestOutsideDistance) {
                greatestOutsideDistance = outsideDistance;
                hasOutsideEdge = true;
                closestEdge = candidate;
                closestArrivalTime = Double.POSITIVE_INFINITY;
            }
            if (hasOutsideEdge && outsideDistance <= 0.0) {
                continue;
            }
            if (speed <= MIN_EDGE_SPEED || distance > 0.05) {
                continue;
            }

            double arrivalTime = Math.max(0.0, distance) / speed;
            if (arrivalTime < closestArrivalTime) {
                closestArrivalTime = arrivalTime;
                closestEdge = candidate;
            }
        }

        return closestEdge;
    }

    private static float rotateLocalYaw(
        final float yaw,
        final Direction currentGravity,
        final Direction edgeDirection,
        final Direction targetGravity
    ) {
        Vec3 worldFacing = RotationUtil.vecPlayerToWorld(RotationUtil.rotToVec(yaw, 0.0F), currentGravity);
        Vec3 targetWorldFacing = rotateAroundEdge(worldFacing, currentGravity, edgeDirection);
        return RotationUtil.vecToRot(RotationUtil.vecWorldToPlayer(targetWorldFacing, targetGravity)).x;
    }

    private static Vec3 directionVector(final Direction direction) {
        return new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    private static double boundary(final BlockPos pos, final Direction direction) {
        double minimum = switch (direction.getAxis()) {
            case X -> pos.getX();
            case Y -> pos.getY();
            case Z -> pos.getZ();
        };
        return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? minimum + 1.0 : minimum;
    }

    private static double component(final Vec3 vector, final Direction.Axis axis) {
        return switch (axis) {
            case X -> vector.x;
            case Y -> vector.y;
            case Z -> vector.z;
        };
    }

    private static Vec3 withComponent(final Vec3 vector, final Direction.Axis axis, final double value) {
        return switch (axis) {
            case X -> new Vec3(value, vector.y, vector.z);
            case Y -> new Vec3(vector.x, value, vector.z);
            case Z -> new Vec3(vector.x, vector.y, value);
        };
    }
}
