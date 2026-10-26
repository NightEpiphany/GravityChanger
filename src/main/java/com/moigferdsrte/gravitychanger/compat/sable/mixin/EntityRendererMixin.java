package com.moigferdsrte.gravitychanger.compat.sable.mixin;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import dev.ryanhcode.sable.Sable;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// A later HEAD injection runs before Sable's cancelling callback.
@Mixin(value = EntityRenderer.class, priority = 900, remap = false)
public abstract class EntityRendererMixin {
    @Shadow
    protected abstract AABB getBoundingBoxForCulling(Entity entity, float partialTicks);

    @Shadow
    protected abstract boolean affectedByCulling(Entity entity);

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$restoreDirectionalFeetForCulling(
        final Entity entity,
        final Frustum culler,
        final double camX,
        final double camY,
        final double camZ,
        final float partialTicks,
        final CallbackInfoReturnable<Boolean> cir
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN
            || !this.affectedByCulling(entity)
            || Sable.HELPER.getTrackingSubLevel(entity) == null
            || Sable.HELPER.getContainingClient(entity) != null) {
            return;
        }

        // Run before Sable's world-Y feet reconstruction can cancel shouldRender.
        Vec3 eyeOffset = RotationUtil.vecPlayerToWorld(0.0, entity.getEyeHeight(), 0.0, gravityDirection);
        Vec3 feetPosition = Sable.HELPER.getEyePositionInterpolated(entity, partialTicks).subtract(eyeOffset);
        AABB bounds = this.getBoundingBoxForCulling(entity, partialTicks).inflate(0.5);
        if (bounds.hasNaN() || bounds.getSize() == 0.0) {
            bounds = AABB.ofSize(entity.position(), 4.0, 4.0, 4.0);
        }

        boolean visible = culler.isVisible(bounds.move(feetPosition.subtract(entity.position())));
        if (!visible && entity instanceof Leashable leashable) {
            Entity leashHolder = leashable.getLeashHolder();
            if (leashHolder != null) {
                visible = culler.isVisible(this.getBoundingBoxForCulling(leashHolder, partialTicks));
            }
        }

        cir.setReturnValue(visible);
    }
}
