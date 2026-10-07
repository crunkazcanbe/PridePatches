package com.dogpound.patches;

/** Logs each kind of scrambled Serene Seasons call once (see MixinSereneSeasonsNullBiome). */
public final class SereneSeasonsLog {
    private SereneSeasonsLog() {}

    private static final java.util.Set<String> SEEN = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public static void once(String method, Object a, Object b) {
        String k = method + "(" + cls(a) + ", " + cls(b) + ")";
        if (SEEN.add(k)) org.apache.logging.log4j.LogManager.getLogger("PridePatches").warn("Serene Seasons got scrambled arguments: {} — answered safely", k);
    }

    private static String cls(Object o) { return o == null ? "null" : o.getClass().getSimpleName(); }
}
