package com.moigferdsrte.gravitychanger.mixin.client;

import com.moigferdsrte.gravitychanger.client.GravityAnimationEntity;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.client.Camera;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jspecify.annotations.Nullable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private @Nullable Entity entity;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    private float eyeHeight;

    @Shadow
    private Vec3 position;

    @Shadow
    @Final
    private Quaternionf rotation;

    protected CameraMixin(Vec3 position) {
        this.position = position;
    }

    @Shadow
    protected abstract void setPosition(final Vec3 position);

    @Inject(
        method = "alignWithEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
            shift = At.Shift.AFTER
        )
    )
    private void gravitychanger$moveEyeAlongLocalUpAfterEntityPosition(final float partialTicks, final CallbackInfo ci) {
        this.gravitychanger$moveEyeAlongLocalUp(partialTicks);
    }

    @Inject(
        method = "alignWithEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setPosition(Lnet/minecraft/world/phys/Vec3;)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void gravitychanger$moveEyeAlongLocalUpAfterMinecartPosition(final float partialTicks, final CallbackInfo ci) {
        this.gravitychanger$moveEyeAlongLocalUp(partialTicks);
    }

    @Inject(
        method = "setRotation",
        at = @At(
            value = "INVOKE",
            target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;",
            shift = At.Shift.AFTER,
            remap = false
        )
    )
    private void gravitychanger$rotateCameraForGravity(final float yRot, final float xRot, final CallbackInfo ci) {
        if (this.entity == null) {
            return;
        }

        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(this.entity);
        Quaternionf gravityRotation = ((GravityAnimationEntity)this.entity)
            .gravitychanger$getVisualGravityRotation(gravityDirection);
        gravityRotation.mul(this.rotation);
        this.rotation.set(gravityRotation);
    }

    @Unique
    private void gravitychanger$moveEyeAlongLocalUp(final float partialTicks) {
        if (this.entity == null) {
            return;
        }

        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(this.entity);
        Quaternionf gravityRotation = ((GravityAnimationEntity)this.entity)
            .gravitychanger$getVisualGravityRotation(gravityDirection);
        if (RotationUtil.isIdentityRotation(gravityRotation)) {
            return;
        }

        Vec3 entityPosition = this.entity.getPosition(partialTicks);
        double eyeHeight = Mth.lerp(partialTicks, this.eyeHeightOld, this.eyeHeight);
        Vec3 eyeOffset = RotationUtil.vecPlayerToWorld(new Vec3(0.0, eyeHeight, 0.0), gravityRotation);
        this.setPosition(entityPosition.add(eyeOffset));
    }
}
