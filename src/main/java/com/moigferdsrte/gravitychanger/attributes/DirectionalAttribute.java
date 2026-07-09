package com.moigferdsrte.gravitychanger.attributes;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class DirectionalAttribute extends RangedAttribute {
    public DirectionalAttribute(String descriptionId) {
        super(descriptionId, Direction.DOWN.get3DDataValue(), 0, Direction.values().length - 1);
    }

    @Override
    public double sanitizeValue(final double value) {
        if (Double.isNaN(value)) {
            return Direction.DOWN.get3DDataValue();
        }

        return toDirection(value).get3DDataValue();
    }

    public static double valueOf(final Direction direction) {
        return direction.get3DDataValue();
    }

    public static Direction toDirection(final double value) {
        int id = Mth.clamp((int)Math.round(value), 0, Direction.values().length - 1);
        return Direction.from3DDataValue(id);
    }
}
