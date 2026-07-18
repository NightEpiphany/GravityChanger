package com.moigferdsrte.gravitychanger.client;

import net.minecraft.core.Direction;
import org.joml.Quaternionf;

public interface GravityAnimationEntity {
    Quaternionf gravitychanger$getVisualGravityRotation(Direction gravityDirection);
}
