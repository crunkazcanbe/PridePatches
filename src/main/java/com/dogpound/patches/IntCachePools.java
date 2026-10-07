package com.dogpound.patches;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-thread copy of Minecraft's IntCache pools (see mixin.MixinIntCacheThreadLocal). Lives OUTSIDE the mixin package:
 * classes inside a mixin package can't be loaded at runtime (that was the 02:19 "ClassNotFoundException ...$Pools" crash).
 */
public final class IntCachePools {
    private IntCachePools() {}

    private int size = 256;
    private final List<int[]> freeSmall = new ArrayList<>(), usedSmall = new ArrayList<>(), freeLarge = new ArrayList<>(), usedLarge = new ArrayList<>();
    private static final ThreadLocal<IntCachePools> POOLS = ThreadLocal.withInitial(IntCachePools::new);

    public static int[] get(int n) {
        IntCachePools p = POOLS.get();
        if (n <= 256) {
            int[] a = p.freeSmall.isEmpty() ? new int[256] : p.freeSmall.remove(p.freeSmall.size() - 1);
            p.usedSmall.add(a);
            return a;
        }
        if (n > p.size) {
            p.size = n;
            p.freeLarge.clear();
            p.usedLarge.clear();
            int[] a = new int[p.size];
            p.usedLarge.add(a);
            return a;
        }
        int[] a = p.freeLarge.isEmpty() ? new int[p.size] : p.freeLarge.remove(p.freeLarge.size() - 1);
        p.usedLarge.add(a);
        return a;
    }

    public static void reset() {
        IntCachePools p = POOLS.get();
        if (!p.freeLarge.isEmpty()) p.freeLarge.remove(p.freeLarge.size() - 1);
        if (!p.freeSmall.isEmpty()) p.freeSmall.remove(p.freeSmall.size() - 1);
        p.freeLarge.addAll(p.usedLarge);
        p.freeSmall.addAll(p.usedSmall);
        p.usedLarge.clear();
        p.usedSmall.clear();
    }

    public static String info() {
        IntCachePools p = POOLS.get();
        return "cache: " + p.freeLarge.size() + ", tcache: " + p.freeSmall.size() + ", allocated: " + p.usedLarge.size() + ", tallocated: " + p.usedSmall.size();
    }
}
