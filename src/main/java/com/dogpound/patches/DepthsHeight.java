package com.dogpound.patches;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import net.minecraft.world.World;

/**
 * Depths Update world height (Y -64..320) for mods that hard-code 0..256; vanilla numbers when it isn't installed (Pride Lite).
 * Reflection because Depths Update is built for Java 21+ and this mod compiles for Java 8.
 */
public final class DepthsHeight {
    private DepthsHeight() {}

    private static final MethodHandle EXTENDED, MIN, MAX, FROM_INDEX;
    static {
        MethodHandle e = null, mn = null, mx = null, fi = null;
        try {
            Class<?> hm = Class.forName("sayys.depthsupdate.core.HeightManager");
            MethodHandles.Lookup l = MethodHandles.publicLookup();
            e = l.findStatic(hm, "isExtended", MethodType.methodType(boolean.class, World.class));
            mn = l.findStatic(hm, "getMinY", MethodType.methodType(int.class, World.class));
            mx = l.findStatic(hm, "getMaxY", MethodType.methodType(int.class, World.class));
            fi = l.findStatic(hm, "fromStorageIndex", MethodType.methodType(int.class, World.class, int.class));
        } catch (Throwable ignored) { e = null; }
        EXTENDED = e; MIN = mn; MAX = mx; FROM_INDEX = fi;
    }

    public static boolean on(World w) {
        try { return EXTENDED != null && w != null && (boolean) EXTENDED.invokeExact(w); } catch (Throwable t) { return false; }
    }
    public static int minY(World w) {
        try { return on(w) ? (int) MIN.invokeExact(w) : 0; } catch (Throwable t) { return 0; }
    }
    public static int maxY(World w) {
        try { return on(w) ? (int) MAX.invokeExact(w) : 256; } catch (Throwable t) { return 256; }
    }
    /** lowest block Y of chunk storage slot i (Depths Update stores 0..15 first, then the sections above, then the ones below 0) */
    public static int sectionY(World w, int i) {
        try { return on(w) ? ((int) FROM_INDEX.invokeExact(w, i)) << 4 : i << 4; } catch (Throwable t) { return i << 4; }
    }
}
