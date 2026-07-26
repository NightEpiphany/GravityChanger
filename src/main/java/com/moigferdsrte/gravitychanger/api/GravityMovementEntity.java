package com.moigferdsrte.gravitychanger.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface GravityMovementEntity {
    Vec3 gravitychanger$getLastMoveDelta();

    void gravitychanger$rememberGravityCore(BlockPos pos, int tick);

    @Nullable BlockPos gravitychanger$getLastGravityCore();

    int gravitychanger$getLastGravityCoreTick();

    void gravitychanger$clearGravityCore();
}
