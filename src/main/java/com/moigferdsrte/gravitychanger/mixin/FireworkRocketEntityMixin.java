package com.moigferdsrte.gravitychanger.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moigferdsrte.gravitychanger.util.ElytraFlightUtil;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {
    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private void gravitychanger$applyDirectionalElytraBoost(
        final LivingEntity entity,
        final Vec3 vanillaMovement,
        final Operation<Void> operation
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            operation.call(entity, vanillaMovement);
            return;
        }

        Vec3 movement = ElytraFlightUtil.applyFireworkBoost(
            entity.getDeltaMovement(),
            entity.getLookAngle(),
            gravityDirection
        );
        operation.call(entity, movement);
    }
}
