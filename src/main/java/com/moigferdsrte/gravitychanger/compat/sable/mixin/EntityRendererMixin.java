package com.moigferdsrte.gravitychanger.compat.sable.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EntityRenderer.class, priority = 1100, remap = false)
public abstract class EntityRendererMixin {
    @WrapOperation(
        // Sable's shouldRender callback is renamed when merged; select its unique signature.
        method = "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;"
            + "DDDFLorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;subtract(DDD)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 gravitychanger$restoreDirectionalFeetForCulling(
        final Vec3 eyePosition,
        final double x,
        final double y,
        final double z,
        final Operation<Vec3> operation,
        @Local(argsOnly = true) final Entity entity
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return operation.call(eyePosition, x, y, z);
        }

        Vec3 eyeOffset = RotationUtil.vecPlayerToWorld(x, y, z, gravityDirection);
        return operation.call(eyePosition, eyeOffset.x, eyeOffset.y, eyeOffset.z);
    }
}
