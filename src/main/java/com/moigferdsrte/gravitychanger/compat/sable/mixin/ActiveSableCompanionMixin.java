package com.moigferdsrte.gravitychanger.compat.sable.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "dev.ryanhcode.sable.ActiveSableCompanion", remap = false)
public abstract class ActiveSableCompanionMixin {
    @ModifyExpressionValue(
        method = "getEyePositionInterpolated",
        at = @At(value = "NEW", target = "(DDD)Lorg/joml/Vector3d;"),
        require = 2,
        expect = 2
    )
    private Vector3d gravitychanger$rotateEyePosition(
        final Vector3d eyePosition,
        @Local(argsOnly = true, name = "entity") final Entity entity
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return eyePosition;
        }

        double eyeHeight = entity.getEyeHeight();
        Vec3 eyeOffset = RotationUtil.vecPlayerToWorld(0.0, eyeHeight, 0.0, gravityDirection);
        // Correct both tick endpoints before Sable interpolates through the platform poses.
        return eyePosition.add(eyeOffset.x, eyeOffset.y - eyeHeight, eyeOffset.z);
    }
}
