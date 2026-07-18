package com.moigferdsrte.gravitychanger.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GravityChangerConfigTest {
    @Test
    void defaultsEnableTenSecondLimit() {
        GravityChangerConfig config = GravityChangerConfig.defaults();

        assertTrue(config.directionalFallLimitEnabled());
        assertEquals(10, config.directionalFallLimitSeconds());
    }

    @Test
    void maximumFallTimeIsClampedToSupportedRange() {
        assertEquals(5, new GravityChangerConfig(true, 1).directionalFallLimitSeconds());
        assertEquals(30, new GravityChangerConfig(true, 100).directionalFallLimitSeconds());
    }
}
