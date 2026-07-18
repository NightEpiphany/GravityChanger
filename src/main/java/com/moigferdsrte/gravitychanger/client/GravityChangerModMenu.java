package com.moigferdsrte.gravitychanger.client;

import com.moigferdsrte.gravitychanger.config.GravityChangerConfig;
import com.moigferdsrte.gravitychanger.config.GravityChangerConfigManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class GravityChangerModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return GravityChangerModMenu::createScreen;
    }

    private static Screen createScreen(final Screen parent) {
        GravityChangerConfig current = GravityChangerConfigManager.get();
        boolean[] enabled = {current.directionalFallLimitEnabled()};
        int[] seconds = {current.directionalFallLimitSeconds()};

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("config.gravity_changer.title"));
        ConfigCategory category = builder.getOrCreateCategory(
            Component.translatable("config.gravity_changer.category.safety")
        );
        ConfigEntryBuilder entries = builder.entryBuilder();

        category.addEntry(entries.startBooleanToggle(
                Component.translatable("config.gravity_changer.directional_fall_limit_enabled"),
                current.directionalFallLimitEnabled()
            )
            .setDefaultValue(GravityChangerConfig.DEFAULT_DIRECTIONAL_FALL_LIMIT_ENABLED)
            .setTooltip(Component.translatable("config.gravity_changer.directional_fall_limit_enabled.tooltip"))
            .setSaveConsumer(value -> enabled[0] = value)
            .build());
        category.addEntry(entries.startIntSlider(
                Component.translatable("config.gravity_changer.directional_fall_limit_seconds"),
                current.directionalFallLimitSeconds(),
                GravityChangerConfig.MIN_DIRECTIONAL_FALL_LIMIT_SECONDS,
                GravityChangerConfig.MAX_DIRECTIONAL_FALL_LIMIT_SECONDS
            )
            .setDefaultValue(GravityChangerConfig.DEFAULT_DIRECTIONAL_FALL_LIMIT_SECONDS)
            .setTextGetter(value -> Component.translatable("config.gravity_changer.seconds", value))
            .setTooltip(Component.translatable("config.gravity_changer.directional_fall_limit_seconds.tooltip"))
            .setSaveConsumer(value -> seconds[0] = value)
            .build());

        builder.setSavingRunnable(() -> GravityChangerConfigManager.save(
            new GravityChangerConfig(enabled[0], seconds[0])
        ));
        return builder.build();
    }
}
