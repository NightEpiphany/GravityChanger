package com.moigferdsrte.gravitychanger.client;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class GravityLeashRenderUtil {
    private GravityLeashRenderUtil() {
    }

    public static void updateLeashEndpoints(
        final Entity entity,
        final EntityRenderState state,
        final float partialTicks,
        final Quaternionf entityRotation
    ) {
        if (state.leashStates == null || !(entity instanceof Leashable leashable)) {
            return;
        }

        Entity holder = leashable.getLeashHolder();
        if (holder == null) {
            return;
        }

        Vec3 entityPosition = entity.getPosition(partialTicks);
        Vec3 holderPosition = holder.getPosition(partialTicks);
        Direction holderGravity = GravityDirectionUtil.getGravityDirection(holder);
        Quaternionf holderRotation = ((GravityAnimationEntity)holder)
            .gravitychanger$getVisualGravityRotation(holderGravity);
        boolean singlePlayerConnection = state.leashStates.size() == 1 && holder instanceof Player;

        for (EntityRenderState.LeashState leashState : state.leashStates) {
            Vec3 worldEntityOffset = RotationUtil.vecPlayerToWorld(leashState.offset, entityRotation);
            Vec3 localHolderOffset = singlePlayerConnection
                ? getPlayerHoldOffset((Player)holder, partialTicks, holderGravity)
                : leashState.end.subtract(holderPosition);
            Vec3 worldHolderOffset = RotationUtil.vecPlayerToWorld(localHolderOffset, holderRotation);

            leashState.offset = worldEntityOffset;
            leashState.start = entityPosition.add(worldEntityOffset);
            leashState.end = holderPosition.add(worldHolderOffset);
        }
    }

    private static Vec3 getPlayerHoldOffset(
        final Player player,
        final float partialTicks,
        final Direction gravityDirection
    ) {
        double xOffset = 0.22 * (player.getMainArm() == HumanoidArm.RIGHT ? -1.0 : 1.0);
        float xRotation = Mth.lerp(partialTicks * 0.5F, player.getXRot(), player.xRotO) * (float)(Math.PI / 180.0);
        float yRotation = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot) * (float)(Math.PI / 180.0);

        if (player.isFallFlying() || player.isAutoSpinAttack()) {
            Vec3 localLook = RotationUtil.vecWorldToPlayer(player.getViewVector(partialTicks), gravityDirection);
            Vec3 localMovement = RotationUtil.vecWorldToPlayer(player.getDeltaMovement(), gravityDirection);
            double movementLength = localMovement.horizontalDistanceSqr();
            double lookLength = localLook.horizontalDistanceSqr();
            float zRotation = 0.0F;
            if (movementLength > 0.0 && lookLength > 0.0) {
                double dot = (localMovement.x * localLook.x + localMovement.z * localLook.z)
                    / Math.sqrt(movementLength * lookLength);
                double sign = localMovement.x * localLook.z - localMovement.z * localLook.x;
                zRotation = (float)(Math.signum(sign) * Math.acos(Mth.clamp(dot, -1.0, 1.0)));
            }
            return new Vec3(xOffset, -0.11, 0.85)
                .zRot(-zRotation)
                .xRot(-xRotation)
                .yRot(-yRotation);
        }

        if (player.isVisuallySwimming()) {
            return new Vec3(xOffset, 0.2, -0.15).xRot(-xRotation).yRot(-yRotation);
        }

        double yOffset = player.getBbHeight() - 1.0;
        double zOffset = player.isCrouching() ? -0.2 : 0.07;
        return new Vec3(xOffset, yOffset, zOffset).yRot(-yRotation);
    }
}
