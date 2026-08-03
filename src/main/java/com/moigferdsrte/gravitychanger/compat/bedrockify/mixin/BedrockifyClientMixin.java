package com.moigferdsrte.gravitychanger.compat.bedrockify.mixin;

import com.moigferdsrte.gravitychanger.compat.bedrockify.BedrockifyFlyingMomentumCompat;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "me.juancarloscp52.bedrockify.client.BedrockifyClient", remap = false)
public abstract class BedrockifyClientMixin {
    @Redirect(
        method = "lambda$onInitializeClient$2",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;setDeltaMovement(DDD)V",
            ordinal = 0,
            remap = true
        ),
        remap = false
    )
    private void gravitychanger$clearLocalHorizontalMomentum(
        final LocalPlayer player,
        final double x,
        final double y,
        final double z
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(player);
        Vec3 movement = BedrockifyFlyingMomentumCompat.clearLocalHorizontal(
            player.getDeltaMovement(),
            gravityDirection
        );
        player.setDeltaMovement(movement);
    }

    @Redirect(
        method = "lambda$onInitializeClient$2",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;setDeltaMovement(DDD)V",
            ordinal = 1,
            remap = true
        ),
        remap = false
    )
    private void gravitychanger$clearLocalVerticalMomentum(
        final LocalPlayer player,
        final double x,
        final double y,
        final double z
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(player);
        Vec3 movement = BedrockifyFlyingMomentumCompat.clearLocalVertical(
            player.getDeltaMovement(),
            gravityDirection
        );
        player.setDeltaMovement(movement);
    }
}
