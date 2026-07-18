package com.moigferdsrte.gravitychanger.mixin.client;

import com.moigferdsrte.gravitychanger.client.GravityAnimationEntity;
import com.moigferdsrte.gravitychanger.client.GravityRotationAnimation;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityGravityAnimationMixin implements GravityAnimationEntity {
    @Unique
    private final GravityRotationAnimation gravitychanger$gravityRotationAnimation = new GravityRotationAnimation();

    @Override
    public Quaternionf gravitychanger$getVisualGravityRotation(final Direction gravityDirection) {
        // 物理方向立即生效，只有客户端视觉姿态延迟插值。
        return this.gravitychanger$gravityRotationAnimation.getRotation(gravityDirection);
    }
}
