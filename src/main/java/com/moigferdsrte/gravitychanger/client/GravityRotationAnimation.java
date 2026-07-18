package com.moigferdsrte.gravitychanger.client;

import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;

public final class GravityRotationAnimation {
    static final long DURATION_NANOS = 1_250_000_000L;

    private final Quaternionf startRotation = new Quaternionf();
    private final Quaternionf targetRotation = new Quaternionf();
    private Direction targetDirection;
    private long startTimeNanos;
    private boolean initialized;

    public synchronized Quaternionf getRotation(final Direction gravityDirection) {
        return this.getRotation(gravityDirection, System.nanoTime());
    }

    synchronized Quaternionf getRotation(final Direction gravityDirection, final long nowNanos) {
        if (!this.initialized) {
            Quaternionf initialRotation = RotationUtil.getEntityRotationQuaternion(gravityDirection);
            this.startRotation.set(initialRotation);
            this.targetRotation.set(initialRotation);
            this.targetDirection = gravityDirection;
            this.startTimeNanos = nowNanos;
            this.initialized = true;
            return initialRotation;
        }

        if (gravityDirection != this.targetDirection) {
            this.startRotation.set(this.interpolate(nowNanos));
            this.targetRotation.set(RotationUtil.getEntityRotationQuaternion(gravityDirection));
            this.targetDirection = gravityDirection;
            this.startTimeNanos = nowNanos;
        }

        return this.interpolate(nowNanos);
    }

    private Quaternionf interpolate(final long nowNanos) {
        double elapsed = Math.max(0L, nowNanos - this.startTimeNanos);
        float progress = (float)Math.min(1.0, elapsed / DURATION_NANOS);
        float smoothedProgress = progress * progress * (3.0F - 2.0F * progress);
        return new Quaternionf(this.startRotation).slerp(this.targetRotation, smoothedProgress);
    }
}
