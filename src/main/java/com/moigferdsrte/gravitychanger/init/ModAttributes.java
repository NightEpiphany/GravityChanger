package com.moigferdsrte.gravitychanger.init;

import com.moigferdsrte.gravitychanger.GravityChanger;
import com.moigferdsrte.gravitychanger.attributes.DirectionalAttribute;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.moigferdsrte.gravitychanger.GravityChanger.MOD_ID;

public final class ModAttributes {
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Holder<Attribute> GRAVITY_DIRECTION = register(
            new DirectionalAttribute("attribute.name.gravity_changer.gravity_direction")
            .setSyncable(true)
            .setSentiment(Attribute.Sentiment.NEUTRAL),
            "gravity_direction"
    );
    public static final Holder<Attribute> GRAVITY_STRENGTH = register(
            new RangedAttribute("attribute.name.gravity_changer.gravity_strength", 9.8, 5.0, 15.0)
            .setSyncable(true)
            .setSentiment(Attribute.Sentiment.NEUTRAL),
            "gravity_strength"
    );

    private static Holder<Attribute> register(final Attribute attribute, String name) {
        return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, GravityChanger.id(name), attribute);
    }

    public static void init() {
        LOGGER.info("Registering Attributes for " + MOD_ID);
    }
}
