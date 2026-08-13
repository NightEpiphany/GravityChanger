package com.moigferdsrte.gravitychanger.mixin;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LookControl.class)
public abstract class LookControlMixin {
    @Shadow @Final protected Mob mob;
    @Shadow protected double wantedX;
    @Shadow protected double wantedY;
    @Shadow protected double wantedZ;

    @Inject(method = "getXRotD", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$getLocalPitch(final CallbackInfoReturnable<Optional<Float>> cir) {
        Vec3 localDelta = this.gravitychanger$getLocalLookDelta();
        if (localDelta == null) {
            return;
        }

        double horizontalDistance = Math.sqrt(localDelta.x * localDelta.x + localDelta.z * localDelta.z);
        cir.setReturnValue(Math.abs(localDelta.y) <= 1.0E-5F && Math.abs(horizontalDistance) <= 1.0E-5F
            ? Optional.empty()
            : Optional.of((float)(-(Mth.atan2(localDelta.y, horizontalDistance) * 180.0F / Math.PI))));
    }

    @Inject(method = "getYRotD", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$getLocalYaw(final CallbackInfoReturnable<Optional<Float>> cir) {
        Vec3 localDelta = this.gravitychanger$getLocalLookDelta();
        if (localDelta == null) {
            return;
        }

        cir.setReturnValue(Math.abs(localDelta.x) <= 1.0E-5F && Math.abs(localDelta.z) <= 1.0E-5F
            ? Optional.empty()
            : Optional.of((float)(Mth.atan2(localDelta.z, localDelta.x) * 180.0F / Math.PI) - 90.0F));
    }

    @Unique
    private Vec3 gravitychanger$getLocalLookDelta() {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(this.mob);
        if (gravityDirection == Direction.DOWN) {
            return null;
        }

        Vec3 target = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
        return RotationUtil.vecWorldToPlayer(target.subtract(this.mob.getEyePosition()), gravityDirection);
    }
}
