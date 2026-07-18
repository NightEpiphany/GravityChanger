package com.moigferdsrte.gravitychanger.util;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectionalFallTrackerTest {
    @Test
    void triggersAtConfiguredLimitAndRemainsTriggered() {
        DirectionalFallTracker tracker = new DirectionalFallTracker();

        assertFalse(tracker.tick(Direction.WEST, true, 3));
        assertFalse(tracker.tick(Direction.WEST, true, 3));
        assertTrue(tracker.tick(Direction.WEST, true, 3));
        assertTrue(tracker.tick(Direction.WEST, true, 3));
    }

    @Test
    void interruptedFallRestartsTimer() {
        DirectionalFallTracker tracker = new DirectionalFallTracker();

        tracker.tick(Direction.UP, true, 2);
        tracker.tick(Direction.UP, false, 2);

        assertFalse(tracker.tick(Direction.UP, true, 2));
        assertTrue(tracker.tick(Direction.UP, true, 2));
    }

    @Test
    void gravityDirectionChangeRestartsTimer() {
        DirectionalFallTracker tracker = new DirectionalFallTracker();

        tracker.tick(Direction.NORTH, true, 2);

        assertFalse(tracker.tick(Direction.SOUTH, true, 2));
        assertTrue(tracker.tick(Direction.SOUTH, true, 2));
    }

    @Test
    void downGravityNeverUsesDirectionalLimit() {
        DirectionalFallTracker tracker = new DirectionalFallTracker();

        assertFalse(tracker.tick(Direction.DOWN, true, 1));
    }
}
