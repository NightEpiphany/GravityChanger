package com.moigferdsrte.gravitychanger.config;

public record GravityChangerConfig(
    boolean directionalFallLimitEnabled,
    int directionalFallLimitSeconds
) {
    public static final boolean DEFAULT_DIRECTIONAL_FALL_LIMIT_ENABLED = true;
    public static final int DEFAULT_DIRECTIONAL_FALL_LIMIT_SECONDS = 10;
    public static final int MIN_DIRECTIONAL_FALL_LIMIT_SECONDS = 5;
    public static final int MAX_DIRECTIONAL_FALL_LIMIT_SECONDS = 30;

    public GravityChangerConfig {
        directionalFallLimitSeconds = Math.clamp(
            directionalFallLimitSeconds,
            MIN_DIRECTIONAL_FALL_LIMIT_SECONDS,
            MAX_DIRECTIONAL_FALL_LIMIT_SECONDS
        );
    }

    public static GravityChangerConfig defaults() {
        return new GravityChangerConfig(
            DEFAULT_DIRECTIONAL_FALL_LIMIT_ENABLED,
            DEFAULT_DIRECTIONAL_FALL_LIMIT_SECONDS
        );
    }
}
