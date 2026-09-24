package com.moigferdsrte.gravitychanger.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @ModifyVariable(method = "handlePlayerPositionChange(DDDFFZZ)V", at = @At("STORE"), name = "movedUpwards")
    private boolean gravitychanger$useLocalUpwardMovement(
        final boolean movedUpwards,
        @Local(name = "xDist") final double xDist,
        @Local(name = "yDist") final double yDist,
        @Local(name = "zDist") final double zDist
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(this.player);
        if (gravityDirection == Direction.DOWN) {
            return movedUpwards;
        }

        Vec3 worldMovement = new Vec3(xDist, yDist, zDist);
        return GravityDirectionUtil.isMovingAgainstGravity(worldMovement, gravityDirection);
    }
}
