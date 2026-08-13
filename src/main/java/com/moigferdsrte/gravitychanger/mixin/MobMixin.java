package com.moigferdsrte.gravitychanger.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moigferdsrte.gravitychanger.entity.ai.DirectionalGroundPathNavigation;
import com.moigferdsrte.gravitychanger.entity.ai.DirectionalMobAiUtil;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Shadow
    protected PathNavigation navigation;

    @Unique
    private boolean gravitychanger$usesVanillaGroundNavigation;

    @WrapOperation(
        method = "getAttackBoundingBox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"
        )
    )
    private AABB gravitychanger$expandAttackBoxInMovementPlane(
        final AABB box,
        final double x,
        final double y,
        final double z,
        final Operation<AABB> operation
    ) {
        Mob mob = (Mob)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(mob);
        if (gravityDirection == Direction.DOWN) {
            return operation.call(box, x, y, z);
        }

        Vec3 inflation = DirectionalMobAiUtil.getAttackBoxInflation(x, gravityDirection);
        return operation.call(box, inflation.x, inflation.y, inflation.z);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void gravitychanger$updateDirectionalNavigation(final CallbackInfo ci) {
        Mob mob = (Mob)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(mob);

        if (this.navigation.getClass() == GroundPathNavigation.class) {
            this.gravitychanger$usesVanillaGroundNavigation = true;
        } else if (!(this.navigation instanceof DirectionalGroundPathNavigation)) {
            this.gravitychanger$usesVanillaGroundNavigation = false;
        }

        if (gravityDirection == Direction.DOWN) {
            if (this.navigation instanceof DirectionalGroundPathNavigation) {
                this.navigation = this.gravitychanger$newGroundNavigation(mob);
            }
            return;
        }

        if (this.gravitychanger$usesVanillaGroundNavigation
            && (!(this.navigation instanceof DirectionalGroundPathNavigation directional)
                || directional.gravityDirection() != gravityDirection)) {
            this.navigation = this.gravitychanger$newDirectionalNavigation(mob, gravityDirection);
        }
    }

    @Unique
    private GroundPathNavigation gravitychanger$newGroundNavigation(final Mob mob) {
        GroundPathNavigation replacement = new GroundPathNavigation(mob, mob.level());
        this.gravitychanger$copyNavigationCapabilities(replacement);
        return replacement;
    }

    @Unique
    private DirectionalGroundPathNavigation gravitychanger$newDirectionalNavigation(
        final Mob mob,
        final Direction gravityDirection
    ) {
        DirectionalGroundPathNavigation replacement = new DirectionalGroundPathNavigation(mob, mob.level(), gravityDirection);
        this.gravitychanger$copyNavigationCapabilities(replacement);
        return replacement;
    }

    @Unique
    private void gravitychanger$copyNavigationCapabilities(final GroundPathNavigation replacement) {
        NodeEvaluator source = this.navigation.getNodeEvaluator();
        replacement.setCanFloat(source.canFloat());
        replacement.setCanOpenDoors(source.canOpenDoors());
        replacement.getNodeEvaluator().setCanPassDoors(source.canPassDoors());
        replacement.setCanWalkOverFences(source.canWalkOverFences());
    }
}
