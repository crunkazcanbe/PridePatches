package com.dogpound.patches;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import zone.rong.mixinbooter.IEarlyMixinLoader;

/**
 * DogPoundPatches — one coremod for all of the Pride pack's runtime mod fixes (no mod jars touched).
 * Currently: anti-cascading-worldgen guard (kills the world-freeze-when-flying caused by
 * Industrial Upgrade / GregTech reaching into unloaded chunks during population).
 * Add future fixes by dropping more mixins into com.dogpound.patches.mixin + the config.
 */
@IFMLLoadingPlugin.Name("DogPound Patches")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
public class DogPoundPatches implements IFMLLoadingPlugin, IEarlyMixinLoader {

    private static final String CONFIG = "dogpoundpatches.mixins.json";

    @Override public void injectData(Map<String, Object> data) {
        // Register EARLY so the mixins catch GameRegistry/World, which load very early.
        // Safe now: targets are core Forge/vanilla classes only (no mod class to poison).
        try { MixinBootstrap.init(); Mixins.addConfiguration(CONFIG); } catch (Throwable ignored) {}
    }

    @Override public List<String> getMixinConfigs() { return Arrays.asList(CONFIG); }

    @Override public String[] getASMTransformerClass() { return new String[0]; }
    @Override public String getModContainerClass() { return null; }
    @Override public String getSetupClass() { return null; }
    @Override public String getAccessTransformerClass() { return null; }
}
