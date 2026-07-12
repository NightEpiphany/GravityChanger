package com.moigferdsrte.gravitychanger.mixin;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Shadow
    @Final
    private Abilities abilities;

    @Shadow
    protected abstract boolean isStayingOnGroundSurface();

    @Unique
    private double gravitychanger$localVerticalVelocity;

    @Inject(method = "travel", at = @At("HEAD"))
    private void gravitychanger$captureLocalVerticalVelocity(final Vec3 input, final CallbackInfo ci) {
        Player player = (Player)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(player);
        this.gravitychanger$localVerticalVelocity = RotationUtil.vecWorldToPlayer(player.getDeltaMovement(), gravityDirection).y;
    }

    @Redirect(
        method = "travel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;with(Lnet/minecraft/core/Direction$Axis;D)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 gravitychanger$preserveCreativeFlightLocalVerticalVelocity(
        final Vec3 movement,
        final Direction.Axis axis,
        final double value
    ) {
        Player player = (Player)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return movement.with(axis, value);
        }

        Vec3 localMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        return RotationUtil.vecPlayerToWorld(localMovement.x, this.gravitychanger$localVerticalVelocity * 0.6, localMovement.z, gravityDirection);
    }

    @Redirect(
        method = "canPlayerFitWithinBlocksAndEntitiesWhen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/EntityDimensions;makeBoundingBox(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;"
        )
    )
    private AABB gravitychanger$makeDirectionalPoseBoundingBox(final EntityDimensions dimensions, final Vec3 pos) {
        Player player = (Player)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(player);
        return gravityDirection == Direction.DOWN
            ? dimensions.makeBoundingBox(pos)
            : RotationUtil.makeBoxFromDimensions(dimensions, gravityDirection, pos);
    }

    @Inject(
        method = "maybeBackOffFromEdge",
        at = @At("HEAD"),
        cancellable = true
    )
    private void gravitychanger$maybeBackOffFromDirectionalEdge(
        final Vec3 delta,
        final MoverType moverType,
        final CallbackInfoReturnable<Vec3> cir
    ) {
        Player player = (Player)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        Vec3 localDelta = RotationUtil.vecWorldToPlayer(delta, gravityDirection);
        float maxDownStep = player.maxUpStep();
        if (
            !this.abilities.flying
                && !(localDelta.y > 0.0)
                && (moverType == MoverType.SELF || moverType == MoverType.PLAYER)
                && this.isStayingOnGroundSurface()
                && this.gravitychanger$isAboveGround(player, gravityDirection, maxDownStep)
        ) {
            double deltaX = localDelta.x;
            double deltaZ = localDelta.z;
            double step = 0.05;
            double stepX = Math.signum(deltaX) * step;
            double stepZ = Math.signum(deltaZ) * step;

            while (deltaX != 0.0 && this.gravitychanger$canFallAtLeast(player, gravityDirection, deltaX, 0.0, maxDownStep)) {
                if (Math.abs(deltaX) <= step) {
                    deltaX = 0.0;
                    break;
                }

                deltaX -= stepX;
            }

            while (deltaZ != 0.0 && this.gravitychanger$canFallAtLeast(player, gravityDirection, 0.0, deltaZ, maxDownStep)) {
                if (Math.abs(deltaZ) <= step) {
                    deltaZ = 0.0;
                    break;
                }

                deltaZ -= stepZ;
            }

            while (deltaX != 0.0 && deltaZ != 0.0 && this.gravitychanger$canFallAtLeast(player, gravityDirection, deltaX, deltaZ, maxDownStep)) {
                if (Math.abs(deltaX) <= step) {
                    deltaX = 0.0;
                } else {
                    deltaX -= stepX;
                }

                if (Math.abs(deltaZ) <= step) {
                    deltaZ = 0.0;
                } else {
                    deltaZ -= stepZ;
                }
            }

            cir.setReturnValue(RotationUtil.vecPlayerToWorld(deltaX, localDelta.y, deltaZ, gravityDirection));
        } else {
            cir.setReturnValue(delta);
        }
    }

    @Unique
    private boolean gravitychanger$isAboveGround(final Player player, final Direction gravityDirection, final float maxDownStep) {
        return player.onGround()
            || player.fallDistance < maxDownStep
                && !this.gravitychanger$canFallAtLeast(player, gravityDirection, 0.0, 0.0, maxDownStep - player.fallDistance);
    }

    @Unique
    private boolean gravitychanger$canFallAtLeast(
        final Player player,
        final Direction gravityDirection,
        final double deltaX,
        final double deltaZ,
        final double minHeight
    ) {
        Vec3 offset = RotationUtil.vecPlayerToWorld(deltaX, -minHeight, deltaZ, gravityDirection);
        return player.level().noCollision((Entity)(Object)this, player.getBoundingBox().deflate(1.0E-7).move(offset));
    }
}
