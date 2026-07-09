package com.moigferdsrte.gravitychanger.mixin.client;

import com.moigferdsrte.gravitychanger.client.GravityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements GravityRenderState {
    @Unique
    private Direction gravitychanger$gravityDirection = Direction.DOWN;

    @Override
    public Direction gravitychanger$getGravityDirection() {
        return this.gravitychanger$gravityDirection;
    }

    @Override
    public void gravitychanger$setGravityDirection(final Direction direction) {
        this.gravitychanger$gravityDirection = direction;
    }
}
