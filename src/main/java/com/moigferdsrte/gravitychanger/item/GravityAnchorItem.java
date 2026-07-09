package com.moigferdsrte.gravitychanger.item;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Rarity;

public class GravityAnchorItem extends DirectionalGravityItem{
    public GravityAnchorItem(Direction direction, Properties properties) {
        super(direction, properties.rarity(Rarity.EPIC).stacksTo(1));
    }
}
