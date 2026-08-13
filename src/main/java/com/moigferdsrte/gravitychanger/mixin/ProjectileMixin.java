package com.moigferdsrte.gravitychanger.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moigferdsrte.gravitychanger.entity.ai.DirectionalMobAiUtil;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {
    @Unique
    private static final double GRAVITYCHANGER_RANGED_ARC_FACTOR = 0.2;

    @WrapOperation(
        method = "shoot",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/Projectile;getMovementToShoot(DDDFF)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 gravitychanger$shootForDirectionalRangedMob(
        final Projectile projectile,
        final double x,
        final double y,
        final double z,
        final float power,
        final float uncertainty,
        final Operation<Vec3> original
    ) {
        Entity owner = projectile.getOwner();
        if (!(owner instanceof Mob mob) || !(owner instanceof RangedAttackMob)) {
            return original.call(projectile, x, y, z, power, uncertainty);
        }

        LivingEntity target = mob.getTarget();
        Direction shooterGravity = GravityDirectionUtil.getGravityDirection(mob);
        if (target == null || shooterGravity == Direction.DOWN) {
            return original.call(projectile, x, y, z, power, uncertainty);
        }

        Vec3 oldLaunchPosition = projectile.position();
        Vec3 oldTargetPosition = new Vec3(target.getX(), target.getY(1.0 / 3.0), target.getZ());
        Vec3 oldDelta = oldTargetPosition.subtract(oldLaunchPosition);
        Vec3 oldReference = oldDelta.add(0.0, oldDelta.horizontalDistance() * GRAVITYCHANGER_RANGED_ARC_FACTOR, 0.0);

        Vec3 vanillaLaunchBase = new Vec3(mob.getX(), mob.getEyeY() - 0.1, mob.getZ());
        Vec3 vanillaHorizontalOffset = oldLaunchPosition.subtract(vanillaLaunchBase);
        Vec3 launchPosition = DirectionalMobAiUtil.getRangedLaunchPosition(
            mob.getEyePosition(),
            vanillaHorizontalOffset,
            shooterGravity
        );
        Vec3 targetPosition = DirectionalMobAiUtil.getRangedTargetPosition(
            target.position(),
            target.getBbHeight(),
            GravityDirectionUtil.getGravityDirection(target)
        );
        Vec3 correctedReference = DirectionalMobAiUtil.getBallisticDirection(
            launchPosition,
            targetPosition,
            shooterGravity,
            GRAVITYCHANGER_RANGED_ARC_FACTOR
        );
        Vec3 correctedDirection = DirectionalMobAiUtil.rotateBetweenReferences(
            new Vec3(x, y, z),
            oldReference,
            correctedReference
        );

        projectile.setPos(launchPosition);
        return original.call(
            projectile,
            correctedDirection.x,
            correctedDirection.y,
            correctedDirection.z,
            power,
            uncertainty
        );
    }
}
