package com.moigferdsrte.gravitychanger.init;

import com.moigferdsrte.gravitychanger.GravityChanger;
import com.moigferdsrte.gravitychanger.item.*;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static com.moigferdsrte.gravitychanger.GravityChanger.MOD_ID;

public final class ModItems {
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Item GRAVITY_ANCHOR_NORTH = registerItem("gravity_anchor_north", p -> new GravityAnchorItem(Direction.NORTH, p), new Item.Properties());
    public static final Item GRAVITY_ANCHOR_EAST = registerItem("gravity_anchor_east", p -> new GravityAnchorItem(Direction.EAST, p), new Item.Properties());
    public static final Item GRAVITY_ANCHOR_SOUTH = registerItem("gravity_anchor_south", p -> new GravityAnchorItem(Direction.SOUTH, p), new Item.Properties());
    public static final Item GRAVITY_ANCHOR_WEST = registerItem("gravity_anchor_west", p -> new GravityAnchorItem(Direction.WEST, p), new Item.Properties());
    public static final Item GRAVITY_ANCHOR_UP = registerItem("gravity_anchor_up", p -> new GravityAnchorItem(Direction.UP, p), new Item.Properties());
    public static final Item GRAVITY_ANCHOR_DOWN = registerItem("gravity_anchor_down", p -> new GravityAnchorItem(Direction.DOWN, p), new Item.Properties());

    public static final Item GRAVITY_CHANGER_NORTH = registerItem("gravity_changer_north", p -> new GravityChangerItem(Direction.NORTH, p), new Item.Properties());
    public static final Item GRAVITY_CHANGER_EAST = registerItem("gravity_changer_east", p -> new GravityChangerItem(Direction.EAST, p), new Item.Properties());
    public static final Item GRAVITY_CHANGER_SOUTH = registerItem("gravity_changer_south", p -> new GravityChangerItem(Direction.SOUTH, p), new Item.Properties());
    public static final Item GRAVITY_CHANGER_WEST = registerItem("gravity_changer_west", p -> new GravityChangerItem(Direction.WEST, p), new Item.Properties());
    public static final Item GRAVITY_CHANGER_UP = registerItem("gravity_changer_up", p -> new GravityChangerItem(Direction.UP, p), new Item.Properties());
    public static final Item GRAVITY_CHANGER_DOWN = registerItem("gravity_changer_down", p -> new GravityChangerItem(Direction.DOWN, p), new Item.Properties());

    public static final Item GRAVITY_CHANGER_NORTH_AOE = registerItem("gravity_changer_north_aoe", p -> new GravityChangerAOEItem(Direction.NORTH, p), new Item.Properties());
    public static final Item GRAVITY_CHANGER_EAST_AOE = registerItem("gravity_changer_east_aoe", p -> new GravityChangerAOEItem(Direction.EAST, p), new Item.Properties());
    public static final Item GRAVITY_CHANGER_SOUTH_AOE = registerItem("gravity_changer_south_aoe", p -> new GravityChangerAOEItem(Direction.SOUTH, p), new Item.Properties());
    public static final Item GRAVITY_CHANGER_WEST_AOE = registerItem("gravity_changer_west_aoe", p -> new GravityChangerAOEItem(Direction.WEST, p), new Item.Properties());
    public static final Item GRAVITY_CHANGER_UP_AOE = registerItem("gravity_changer_up_aoe", p -> new GravityChangerAOEItem(Direction.UP, p), new Item.Properties());
    public static final Item GRAVITY_CHANGER_DOWN_AOE = registerItem("gravity_changer_down_aoe", p -> new GravityChangerAOEItem(Direction.DOWN, p), new Item.Properties());

    public static final Item GRAVITY_INCREASER = registerItem("gravity_increaser", p -> new GravityModificationItem(ModifiableGravityItem.Modify.INCREASE, p), new Item.Properties());

    public static final Item GRAVITY_DECREASER = registerItem("gravity_decreaser", p -> new GravityModificationItem(ModifiableGravityItem.Modify.DECREASE, p), new Item.Properties());

    public static final Item GRAVITY_INCREASER_AOE = registerItem("gravity_increaser_aoe", p -> new GravityModificationAOEItem(ModifiableGravityItem.Modify.INCREASE, p), new Item.Properties());

    public static final Item GRAVITY_DECREASER_AOE = registerItem("gravity_decreaser_aoe", p -> new GravityModificationAOEItem(ModifiableGravityItem.Modify.DECREASE, p), new Item.Properties());
    private static Item registerItem(final String id, final Function<Item.Properties, Item> itemFactory, final Item.Properties properties) {
        Item item = itemFactory.apply(properties.setId(ResourceKey.create(Registries.ITEM, GravityChanger.id(id))));

        return Registry.register(BuiltInRegistries.ITEM, GravityChanger.id(id), item);
    }
    public static void init() {
        LOGGER.info("Registering Items for " + MOD_ID);
    }
}
