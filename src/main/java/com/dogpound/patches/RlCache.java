package com.dogpound.patches;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.ResourceLocation;

/** One shared ResourceLocation per id string for hot data-walker paths (they're immutable, so sharing is safe). */
public final class RlCache {
    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();

    private RlCache() { }

    public static ResourceLocation of(String id) {
        ResourceLocation rl = CACHE.get(id);
        if (rl == null) {
            if (CACHE.size() > 16384) CACHE.clear(); // ponytail: crude bound, fine for registry-sized id sets
            rl = new ResourceLocation(id);
            CACHE.put(id, rl);
        }
        return rl;
    }
}
