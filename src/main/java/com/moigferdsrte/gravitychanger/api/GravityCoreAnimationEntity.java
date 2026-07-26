package com.moigferdsrte.gravitychanger.api;

import net.minecraft.core.Direction;

/** 实体同步重力核心专用的视觉转换事件。 */
public interface GravityCoreAnimationEntity {
    void gravitychanger$startCoreGravityTransition(Direction previousGravity, Direction targetGravity);
}
