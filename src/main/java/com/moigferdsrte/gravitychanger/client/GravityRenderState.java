package com.moigferdsrte.gravitychanger.client;

import org.joml.Quaternionf;

public interface GravityRenderState {
    Quaternionf gravitychanger$getGravityRotation();

    void gravitychanger$setGravityRotation(Quaternionf rotation);
}
