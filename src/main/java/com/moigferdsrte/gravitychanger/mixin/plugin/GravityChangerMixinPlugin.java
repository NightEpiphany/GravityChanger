package com.moigferdsrte.gravitychanger.mixin.plugin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class GravityChangerMixinPlugin implements IMixinConfigPlugin {
    private static final String BEDROCKIFY_COMPAT_PACKAGE =
        "com.moigferdsrte.gravitychanger.compat.bedrockify.";
    private static final String SEAMLESS_PORTALS_PLAYER_MIXIN =
        "com.moigferdsrte.gravitychanger.mixin.PlayerDirectionalCollisionMixin";
    private static final String SEAMLESS_PORTALS_COMPAT_PACKAGE =
        "com.moigferdsrte.gravitychanger.compat.seamlessportals.";

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(BEDROCKIFY_COMPAT_PACKAGE)) {
            return FabricLoader.getInstance().isModLoaded("bedrockify");
        }

        if (mixinClassName.equals(SEAMLESS_PORTALS_PLAYER_MIXIN)) {
            return !FabricLoader.getInstance().isModLoaded("seamlessportals");
        }

        if (mixinClassName.startsWith(SEAMLESS_PORTALS_COMPAT_PACKAGE)) {
            return FabricLoader.getInstance().isModLoaded("seamlessportals");
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
