package com.moigferdsrte.gravitychanger.mixin.client;

import com.moigferdsrte.gravitychanger.client.GravityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements GravityRenderState {
    @Unique
    private final Quaternionf gravitychanger$gravityRotation = new Quaternionf();

    @Override
    public Quaternionf gravitychanger$getGravityRotation() {
        return this.gravitychanger$gravityRotation;
    }

    @Override
    public void gravitychanger$setGravityRotation(final Quaternionf rotation) {
        this.gravitychanger$gravityRotation.set(rotation);
    }
}
