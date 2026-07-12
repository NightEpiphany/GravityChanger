package com.moigferdsrte.gravitychanger.init;

import com.moigferdsrte.gravitychanger.GravityChanger;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

public final class ModCreativeTabs {
    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceKey.create(Registries.CREATIVE_MODE_TAB, GravityChanger.id("gravity_changer")), FabricCreativeModeTab.builder()
                .title(Component.translatable("item_group.gravity_changer.name"))
                .icon(ModItems.GRAVITY_ANCHOR_DOWN::getDefaultInstance)
                .displayItems((_, output) -> {
                    output.accept(ModItems.GRAVITY_CHANGER_NORTH);
                    output.accept(ModItems.GRAVITY_CHANGER_EAST);
                    output.accept(ModItems.GRAVITY_CHANGER_SOUTH);
                    output.accept(ModItems.GRAVITY_CHANGER_WEST);
                    output.accept(ModItems.GRAVITY_CHANGER_UP);
                    output.accept(ModItems.GRAVITY_CHANGER_DOWN);
                    output.accept(ModItems.GRAVITY_INCREASER);
                    output.accept(ModItems.GRAVITY_DECREASER);

                    output.accept(ModItems.GRAVITY_CHANGER_NORTH_AOE);
                    output.accept(ModItems.GRAVITY_CHANGER_EAST_AOE);
                    output.accept(ModItems.GRAVITY_CHANGER_SOUTH_AOE);
                    output.accept(ModItems.GRAVITY_CHANGER_WEST_AOE);
                    output.accept(ModItems.GRAVITY_CHANGER_UP_AOE);
                    output.accept(ModItems.GRAVITY_CHANGER_DOWN_AOE);
                    output.accept(ModItems.GRAVITY_INCREASER_AOE);
                    output.accept(ModItems.GRAVITY_DECREASER_AOE);

                    output.accept(ModItems.GRAVITY_ANCHOR_NORTH);
                    output.accept(ModItems.GRAVITY_ANCHOR_EAST);
                    output.accept(ModItems.GRAVITY_ANCHOR_SOUTH);
                    output.accept(ModItems.GRAVITY_ANCHOR_WEST);
                    output.accept(ModItems.GRAVITY_ANCHOR_UP);
                    output.accept(ModItems.GRAVITY_ANCHOR_DOWN);
                }).build());
    }
}
