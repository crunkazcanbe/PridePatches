package com.dogpound.patches;

/** Thread-local flag: true only while the modded worldgen pass is running. */
public final class GenGuard {
    public static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> Boolean.FALSE);
    public static long blocked = 0; // diagnostic counter: cross-chunk worldgen accesses we stopped
    private GenGuard() {}
}
