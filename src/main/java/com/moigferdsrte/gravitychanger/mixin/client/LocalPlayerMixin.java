package com.moigferdsrte.gravitychanger.mixin.client;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$skipWorldHorizontalPushOutForSideGravity(final double x, final double z, final CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer)(Object)this;
        if (GravityDirectionUtil.getGravityDirection(player).getAxis().isHorizontal()) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "aiStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;",
            ordinal = 0
        )
    )
    private Vec3 gravitychanger$moveCreativeFlightVertically(final Vec3 movement, final double x, final double y, final double z) {
        LocalPlayer player = (LocalPlayer)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(player);
        return gravityDirection == Direction.DOWN ? movement.add(x, y, z) : movement.add(RotationUtil.vecPlayerToWorld(x, y, z, gravityDirection));
    }
}
