package com.moigferdsrte.gravitychanger.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moigferdsrte.gravitychanger.entity.ai.DirectionalGroundNodeEvaluator;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MoveControl.class)
public abstract class MoveControlMixin {
    @Unique
    private static final double GRAVITYCHANGER_MINIMUM_JUMP_HEIGHT = 0.5;

    @Shadow @Final protected Mob mob;
    @Shadow protected abstract float rotlerp(float current, float target, float maximumChange);

    @WrapOperation(
        method = "isWalkable",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;"
        )
    )
    private BlockPos gravitychanger$getDirectionalStrafePosition(
        final double x,
        final double y,
        final double z,
        final Operation<BlockPos> original
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(this.mob);
        if (gravityDirection == Direction.DOWN) {
            return original.call(x, y, z);
        }

        Vec3 localOffset = new Vec3(x - this.mob.getX(), 0.0, z - this.mob.getZ());
        Vec3 worldPosition = this.mob.position().add(RotationUtil.vecPlayerToWorld(localOffset, gravityDirection));
        return DirectionalGroundNodeEvaluator.nodePosition(worldPosition, gravityDirection);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$moveInLocalCoordinates(final CallbackInfo ci) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(this.mob);
        MoveControl<?> control = (MoveControl<?>)(Object)this;
        if (gravityDirection == Direction.DOWN || !control.hasWanted()) {
            return;
        }

        ci.cancel();
        Vec3 target = new Vec3(control.getWantedX(), control.getWantedY(), control.getWantedZ());
        Vec3 localDelta = RotationUtil.vecWorldToPlayer(target.subtract(this.mob.position()), gravityDirection);
        control.setWait();
        if (localDelta.lengthSqr() < MoveControl.MIN_SPEED_SQR) {
            this.mob.setZza(0.0F);
            return;
        }

        float targetYaw = (float)(Mth.atan2(localDelta.z, localDelta.x) * 180.0F / Math.PI) - 90.0F;
        this.mob.setYRot(this.rotlerp(this.mob.getYRot(), targetYaw, 90.0F));
        this.mob.setSpeed((float)(control.getSpeedModifier() * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));

        double horizontalDistanceSqr = localDelta.x * localDelta.x + localDelta.z * localDelta.z;
        if (this.mob.onGround()
            && localDelta.y > GRAVITYCHANGER_MINIMUM_JUMP_HEIGHT
            && horizontalDistanceSqr < Math.max(1.0F, this.mob.getBbWidth())) {
            this.mob.getJumpControl().jump();
        }
    }
}
