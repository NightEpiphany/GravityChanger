package com.moigferdsrte.gravitychanger.compat.bedrockify;

import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class BedrockifyFlyingMomentumCompat {
    private BedrockifyFlyingMomentumCompat() {
    }

    public static Vec3 clearLocalHorizontal(final Vec3 movement, final Direction gravityDirection) {
        Vec3 localMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        return RotationUtil.vecPlayerToWorld(0.0, localMovement.y, 0.0, gravityDirection);
    }

    public static Vec3 clearLocalVertical(final Vec3 movement, final Direction gravityDirection) {
        Vec3 localMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        return RotationUtil.vecPlayerToWorld(localMovement.x, 0.0, localMovement.z, gravityDirection);
    }
}
