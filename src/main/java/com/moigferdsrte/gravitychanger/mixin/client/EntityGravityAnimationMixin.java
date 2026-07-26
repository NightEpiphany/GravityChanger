package com.moigferdsrte.gravitychanger.mixin.client;

import com.moigferdsrte.gravitychanger.api.GravityCoreAnimationEntity;
import com.moigferdsrte.gravitychanger.api.GravityMovementEntity;
import com.moigferdsrte.gravitychanger.client.GravityAnimationEntity;
import com.moigferdsrte.gravitychanger.client.GravityCoreRotationAnimation;
import com.moigferdsrte.gravitychanger.client.GravityRotationAnimation;
import com.moigferdsrte.gravitychanger.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityGravityAnimationMixin implements GravityAnimationEntity, GravityCoreAnimationEntity {
    @Unique
    private final GravityRotationAnimation gravitychanger$gravityRotationAnimation = new GravityRotationAnimation();

    @Unique
    private final GravityCoreRotationAnimation gravitychanger$coreRotationAnimation = new GravityCoreRotationAnimation();

    @Unique
    private boolean gravitychanger$coreTransitionActive;

    @Unique
    private Direction gravitychanger$observedGravityDirection = Direction.DOWN;

    @Unique
    private boolean gravitychanger$gravityDirectionInitialized;

    @Override
    public void gravitychanger$startCoreGravityTransition(
        final Direction previousGravity,
        final Direction targetGravity
    ) {
        long nowNanos = System.nanoTime();
        this.gravitychanger$coreRotationAnimation.start(previousGravity, targetGravity, nowNanos);
        // 核心动画只覆盖过渡阶段；完成后回到标准重力姿态，保证输入坐标系与视角一致。
        this.gravitychanger$gravityRotationAnimation.forceSet(targetGravity, nowNanos);
        this.gravitychanger$coreTransitionActive = true;
    }

    @Override
    public Quaternionf gravitychanger$getVisualGravityRotation(final Direction gravityDirection) {
        long nowNanos = System.nanoTime();
        if (!this.gravitychanger$gravityDirectionInitialized) {
            this.gravitychanger$observedGravityDirection = gravityDirection;
            this.gravitychanger$gravityDirectionInitialized = true;
        } else if (gravityDirection != this.gravitychanger$observedGravityDirection) {
            if (this.gravitychanger$hasRecentCoreContact()) {
                this.gravitychanger$startCoreGravityTransition(
                    this.gravitychanger$observedGravityDirection,
                    gravityDirection
                );
            }
            this.gravitychanger$observedGravityDirection = gravityDirection;
        }

        if (this.gravitychanger$coreTransitionActive
            && this.gravitychanger$coreRotationAnimation.isActive(nowNanos)) {
            return this.gravitychanger$coreRotationAnimation.getRotation(nowNanos);
        }
        this.gravitychanger$coreTransitionActive = false;
        return this.gravitychanger$gravityRotationAnimation.getRotation(gravityDirection, nowNanos);
    }

    @Unique
    private boolean gravitychanger$hasRecentCoreContact() {
        Entity entity = (Entity)(Object)this;
        if (!(entity instanceof GravityMovementEntity movementEntity)) {
            return false;
        }

        BlockPos corePos = movementEntity.gravitychanger$getLastGravityCore();
        if (corePos == null || entity.tickCount - movementEntity.gravitychanger$getLastGravityCoreTick() > 2) {
            return false;
        }

        return entity.level().getBlockState(corePos).is(ModBlocks.GRAVITY_CORE)
            && entity.distanceToSqr(
                corePos.getX() + 0.5D,
                corePos.getY() + 0.5D,
                corePos.getZ() + 0.5D
            ) < 9.0D;
    }
}
