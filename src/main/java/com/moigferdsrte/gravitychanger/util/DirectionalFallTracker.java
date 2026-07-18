package com.moigferdsrte.gravitychanger.util;

import net.minecraft.core.Direction;

public final class DirectionalFallTracker {
    private Direction trackedDirection = Direction.DOWN;
    private int elapsedTicks;

    public boolean tick(final Direction gravityDirection, final boolean falling, final int maximumTicks) {
        if (!falling || gravityDirection == Direction.DOWN) {
            this.reset();
            return false;
        }

        if (gravityDirection != this.trackedDirection) {
            this.trackedDirection = gravityDirection;
            this.elapsedTicks = 0;
        }

        this.elapsedTicks = Math.min(this.elapsedTicks + 1, maximumTicks);
        return this.elapsedTicks >= maximumTicks;
    }

    public void reset() {
        this.trackedDirection = Direction.DOWN;
        this.elapsedTicks = 0;
    }
}
