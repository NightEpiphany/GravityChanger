package com.moigferdsrte.gravitychanger;

import com.moigferdsrte.gravitychanger.init.ModAttributes;
import com.moigferdsrte.gravitychanger.init.ModCreativeTabs;
import com.moigferdsrte.gravitychanger.init.ModEvents;
import com.moigferdsrte.gravitychanger.init.ModItems;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

public final class GravityChanger implements ModInitializer {
	public static final String MOD_ID = "gravity_changer";

	@Override
	public void onInitialize() {
		ModAttributes.init();
		ModItems.init();
		ModEvents.init();
		ModCreativeTabs.init();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
