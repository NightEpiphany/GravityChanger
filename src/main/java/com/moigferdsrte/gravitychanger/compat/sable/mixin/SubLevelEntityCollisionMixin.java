package com.moigferdsrte.gravitychanger.compat.sable.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.moigferdsrte.gravitychanger.compat.sable.SableCollisionSupportEntity;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import dev.ryanhcode.sable.sublevel.entity_collision.SubLevelEntityCollision;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "dev.ryanhcode.sable.sublevel.entity_collision.SubLevelEntityCollision", remap = false)
public abstract class SubLevelEntityCollisionMixin {
    @Unique
    private static final Vector3dc[] GRAVITY_UP_VECTORS = new Vector3dc[Direction.values().length];

    static {
        for (Direction gravityDirection : Direction.values()) {
            Direction localUp = gravityDirection.getOpposite();
            GRAVITY_UP_VECTORS[gravityDirection.get3DDataValue()] = new Vector3d(
                localUp.getStepX(),
                localUp.getStepY(),
                localUp.getStepZ()
            );
        }
    }

    @ModifyReturnValue(method = "collide", at = @At("RETURN"))
    private static SubLevelEntityCollision.CollisionInfo gravitychanger$rememberSubLevelSupport(
        final SubLevelEntityCollision.CollisionInfo collisionInfo,
        @Local(argsOnly = true, name = "entity") final Entity entity
    ) {
        boolean directionalSupport = GravityDirectionUtil.getGravityDirection(entity) != Direction.DOWN
            && collisionInfo.verticalCollisionBelow;
        ((SableCollisionSupportEntity)entity).gravitychanger$setSableCollisionSupport(directionalSupport);
        return collisionInfo;
    }

    @ModifyExpressionValue(
        method = "collide",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/phys/Vec3;y:D",
            opcode = Opcodes.GETFIELD,
            ordinal = 0
        )
    )
    private static double gravitychanger$readLocalVerticalVelocity(
        final double worldY,
        @Local(argsOnly = true, name = "entity") final Entity entity
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        return gravityDirection == Direction.DOWN
            ? worldY
            : RotationUtil.vecWorldToPlayer(entity.getDeltaMovement(), gravityDirection).y;
    }

    @ModifyExpressionValue(
        method = "collide",
        at = @At(
            value = "FIELD",
            target = "Ldev/ryanhcode/sable/api/math/OrientedBoundingBox3d;UP:Lorg/joml/Vector3dc;",
            opcode = Opcodes.GETSTATIC
        )
    )
    private static Vector3dc gravitychanger$getDirectionalEntityUp(
        final Vector3dc original,
        @Local(argsOnly = true, name = "entity") final Entity entity
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection != Direction.DOWN) {
            return GRAVITY_UP_VECTORS[gravityDirection.get3DDataValue()];
        }

        return original;
    }

    @ModifyExpressionValue(
        method = "collide",
        at = @At(
            value = "INVOKE",
            target = "Ldev/ryanhcode/sable/sublevel/entity_collision/SubLevelEntityCollision;getHitBoxYaw(Ldev/ryanhcode/sable/companion/math/Pose3dc;)D"
        ),
        require = 2,
        expect = 2
    )
    private static double gravitychanger$keepDirectionalBoundsAligned(
        final double yaw,
        @Local(argsOnly = true, name = "entity") final Entity entity
    ) {
        // World-Y yaw would tilt a horizontal player's long axis away from gravity.
        return GravityDirectionUtil.getGravityDirection(entity).getAxis() == Direction.Axis.Y ? yaw : 0.0;
    }

    @WrapOperation(
        method = "collide",
        at = {
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/phys/AABB;getYsize()D",
                ordinal = 3
            ),
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/phys/AABB;getYsize()D",
                ordinal = 4
            )
        },
        require = 2,
        expect = 2
    )
    private static double gravitychanger$getTrackingAnchorHeight(
        final AABB bounds,
        final Operation<Double> operation,
        @Local(argsOnly = true, name = "entity") final Entity entity
    ) {
        // The world AABB is already rotated; only its feet anchor uses local height.
        return gravitychanger$getGravityAxisSize(bounds, entity);
    }

    @WrapOperation(
        method = "getSubLevelEntityCollisionShape",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;getYsize()D"
        )
    )
    private static double gravitychanger$getScaffoldingProbeHeight(
        final AABB bounds,
        final Operation<Double> operation,
        @Local(argsOnly = true, name = "entity") final Entity entity
    ) {
        return gravitychanger$getGravityAxisSize(bounds, entity);
    }

    @Unique
    private static double gravitychanger$getGravityAxisSize(final AABB bounds, final Entity entity) {
        return switch (GravityDirectionUtil.getGravityDirection(entity).getAxis()) {
            case X -> bounds.getXsize();
            case Y -> bounds.getYsize();
            case Z -> bounds.getZsize();
        };
    }

    @SuppressWarnings("JavaExistingMethodCanBeUsed")
    @WrapOperation(
        method = "collide",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private static Vec3 gravitychanger$clearVelocityIntoDirectionalSupport(
        final Vec3 movement,
        final double xScale,
        final double yScale,
        final double zScale,
        final Operation<Vec3> operation,
        @Local(argsOnly = true, name = "entity") final Entity entity
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return operation.call(movement, xScale, yScale, zScale);
        }

        Vec3 localMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        return RotationUtil.vecPlayerToWorld(localMovement.x, 0.0, localMovement.z, gravityDirection);
    }
}
