package com.moigferdsrte.gravitychanger.item;

import net.minecraft.world.item.Item;

public abstract class ModifiableGravityItem extends Item {
    private final Modify modify;

    protected static final double MODIFICATION_STEP = 0.1D;

    public ModifiableGravityItem(Modify modify, Properties properties) {
        this.modify = modify;
        super(properties);
    }

    public Modify getModify() {
        return modify;
    }

    public enum Modify {
        INCREASE,
        DECREASE
    }
}
