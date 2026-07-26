package com.moigferdsrte.gravitychanger;

import com.moigferdsrte.gravitychanger.config.GravityChangerConfigManager;
import com.moigferdsrte.gravitychanger.init.*;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GravityChanger implements ModInitializer {
	public static final String MOD_ID = "gravity_changer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		GravityChangerConfigManager.initialize();
		ModAttributes.init();
		ModEvents.init();
		ModBlocks.init();
		ModItems.init();
		ModCreativeTabs.init();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
