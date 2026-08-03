package com.moigferdsrte.gravitychanger.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Applies the directional player pose box when no portal mod replaces the fit check. */
@Mixin(Player.class)
public abstract class PlayerDirectionalCollisionMixin {
    @WrapOperation(
        method = "canPlayerFitWithinBlocksAndEntitiesWhen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/EntityDimensions;makeBoundingBox(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;"
        )
    )
    private AABB gravitychanger$makeDirectionalPoseBoundingBox(
        final EntityDimensions dimensions,
        final Vec3 pos,
        final Operation<AABB> operation
    ) {
        Player player = (Player)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(player);
        return gravityDirection == Direction.DOWN
            ? operation.call(dimensions, pos)
            : RotationUtil.makeBoxFromDimensions(dimensions, gravityDirection, pos);
    }
}
