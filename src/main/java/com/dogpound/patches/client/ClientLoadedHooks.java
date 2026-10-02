package com.dogpound.patches.client;

import java.util.ArrayList;
import java.util.List;

/** Background jobs mods start too early; started again once the game has finished loading (MixinMinecraftLoaded). */
public final class ClientLoadedHooks {
    private static final List<Object[]> waiting = new ArrayList<>();
    private static boolean loaded;

    private ClientLoadedHooks() {}

    public static synchronized void later(String threadName, Runnable job) {
        if (loaded) { new Thread(job, threadName).start(); return; }
        waiting.add(new Object[]{threadName, job});
        org.apache.logging.log4j.LogManager.getLogger("PridePatches").info("[LoadOrder] holding '" + threadName + "' until the game has finished loading");
    }

    public static synchronized void run() {
        loaded = true;
        for (Object[] w : waiting) {
            org.apache.logging.log4j.LogManager.getLogger("PridePatches").info("[LoadOrder] starting '" + w[0] + "' now");
            new Thread((Runnable) w[1], (String) w[0]).start();
        }
        waiting.clear();
    }
}
