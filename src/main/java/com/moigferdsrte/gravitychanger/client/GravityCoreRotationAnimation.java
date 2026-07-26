package com.moigferdsrte.gravitychanger.client;

import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/** 沿重力核心公共棱边执行固定 90 度旋转的动画。 */
public final class GravityCoreRotationAnimation {
    static final long DURATION_NANOS = 1_000_000_000L;
    private static final float HALF_TURN = (float)(Math.PI * 0.5D);

    private final Quaternionf targetRotation = new Quaternionf();
    private Vec3 rotationAxis = Vec3.ZERO;
    private long startTimeNanos;
    private boolean active;

    public synchronized void start(
        final Direction previousGravity,
        final Direction targetGravity,
        final long nowNanos
    ) {
        Vec3 previous = directionVector(previousGravity);
        Vec3 target = directionVector(targetGravity);
        Vec3 axis = previous.cross(target);
        if (axis.lengthSqr() < 0.5D) {
            this.active = false;
            return;
        }

        this.rotationAxis = axis.normalize();
        this.targetRotation.set(RotationUtil.getEntityRotationQuaternion(targetGravity));
        // 以标准目标姿态为动画锚点，避免结束帧切回通用姿态时发生跳变。
        this.startTimeNanos = nowNanos;
        this.active = true;
    }

    public synchronized Quaternionf getRotation(final long nowNanos) {
        if (!this.active) {
            return new Quaternionf(this.targetRotation);
        }

        double elapsed = Math.max(0L, nowNanos - this.startTimeNanos);
        float progress = (float)Math.min(1.0D, elapsed / (double)DURATION_NANOS);
        float smoothedProgress = progress * progress * (3.0F - 2.0F * progress);
        Quaternionf rotation = new Quaternionf()
            .rotateAxis(
                -HALF_TURN * (1.0F - smoothedProgress),
                (float)this.rotationAxis.x,
                (float)this.rotationAxis.y,
                (float)this.rotationAxis.z
            )
            .mul(this.targetRotation);
        if (progress >= 1.0F) {
            this.active = false;
        }
        return rotation;
    }

    public synchronized boolean isActive(final long nowNanos) {
        if (!this.active) {
            return false;
        }
        return nowNanos - this.startTimeNanos < DURATION_NANOS;
    }

    public synchronized Quaternionf getTargetRotation() {
        return new Quaternionf(this.targetRotation);
    }

    private static Vec3 directionVector(final Direction direction) {
        return new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }
}
