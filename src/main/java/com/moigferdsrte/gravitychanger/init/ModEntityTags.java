package com.moigferdsrte.gravitychanger.init;

import com.moigferdsrte.gravitychanger.GravityChanger;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

@SuppressWarnings("all")
public interface ModEntityTags {
    TagKey<EntityType<?>> GRAVITY_FIXED = create("gravity_fixed");
    private static TagKey<EntityType<?>> create(final String name) {
        return TagKey.create(Registries.ENTITY_TYPE, GravityChanger.id(name));
    }
}
