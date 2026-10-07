package com.dogpound.patches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.world.gen.layer.IntCache;

/**
 * Minecraft's IntCache is a set of static scratch arrays shared by EVERY thread that asks "which biome is here?".
 * In the Pride pack several threads do that at once (HBM's radiation biome check on the client thread, Distant
 * Horizons building LODs in the background, OTG/Subaquatic/BuildCraft biome layers) — they overwrote each other's
 * arrays → "arraycopy: last source index 577 out of bounds for int[576]" in GenLayerVoronoiZoom and garbage biomes.
 * Give every thread its own pool.
 */
@Mixin(value = IntCache.class, remap = false)
public abstract class MixinIntCacheThreadLocal {
    /** @author PridePatches @reason thread-safe per-thread pools (see class comment) */
    @Overwrite
    public static int[] func_76445_a(int n) { return com.dogpound.patches.IntCachePools.get(n); }

    /** @author PridePatches @reason thread-safe per-thread pools */
    @Overwrite
    public static void func_76446_a() { com.dogpound.patches.IntCachePools.reset(); }

    /** @author PridePatches @reason debug string from this thread's pools */
    @Overwrite
    public static String func_85144_b() { return com.dogpound.patches.IntCachePools.info(); }
}
