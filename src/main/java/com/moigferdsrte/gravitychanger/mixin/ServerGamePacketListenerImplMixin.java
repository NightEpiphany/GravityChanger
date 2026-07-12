package com.moigferdsrte.gravitychanger.mixin;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @ModifyVariable(method = "handleMovePlayer", at = @At("STORE"), name = "movedUpwards")
    private boolean gravitychanger$useLocalUpwardMovement(
        final boolean movedUpwards,
        final ServerboundMovePlayerPacket packet
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(this.player);
        if (gravityDirection == Direction.DOWN) {
            return movedUpwards;
        }

        Vec3 worldMovement = new Vec3(
            packet.getX(this.player.getX()) - this.player.getX(),
            packet.getY(this.player.getY()) - this.player.getY(),
            packet.getZ(this.player.getZ()) - this.player.getZ()
        );
        return GravityDirectionUtil.isMovingAgainstGravity(worldMovement, gravityDirection);
    }
}
