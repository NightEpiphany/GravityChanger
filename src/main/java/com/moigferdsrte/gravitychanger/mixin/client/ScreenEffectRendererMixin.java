package com.moigferdsrte.gravitychanger.mixin.client;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
    @Inject(
        method = "getViewBlockingState(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void gravitychanger$getDirectionalViewBlockingState(
        final Player player,
        final CallbackInfoReturnable<BlockState> cir
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        if (player.noPhysics) {
            cir.setReturnValue(null);
            return;
        }

        Vec3 eyePosition = player.getEyePosition();
        double horizontalRadius = player.getBbWidth() * 0.8F;
        double verticalRadius = 0.1F * player.getScale();
        BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 8; i++) {
            double localX = (((i >> 0) & 1) - 0.5F) * horizontalRadius;
            double localY = (((i >> 1) & 1) - 0.5F) * verticalRadius;
            double localZ = (((i >> 2) & 1) - 0.5F) * horizontalRadius;
            Vec3 samplePosition = eyePosition.add(RotationUtil.vecPlayerToWorld(localX, localY, localZ, gravityDirection));
            testPos.set(samplePosition.x, samplePosition.y, samplePosition.z);

            BlockState blockState = player.level().getBlockState(testPos);
            if (blockState.getRenderShape() != RenderShape.INVISIBLE && blockState.isViewBlocking(player.level(), testPos)) {
                cir.setReturnValue(blockState);
                return;
            }
        }

        cir.setReturnValue(null);
    }
}
