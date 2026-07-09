package com.moigferdsrte.gravitychanger.item;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public abstract class DirectionalGravityItem extends Item {
    private final Direction direction;
    public DirectionalGravityItem(Direction direction, Properties properties) {
        super(properties);
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    protected boolean applyGravityDirection(final LivingEntity entity) {
        if (GravityDirectionUtil.getGravityDirection(entity) == this.direction) {
            return false;
        }

        return GravityDirectionUtil.setGravityDirection(entity, this.direction);
    }
}
