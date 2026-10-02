package com.dogpound.patches.mixin;

import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * 2026-10-01: a mod's error during Init froze the game inside LoadController.errorOccurred (runnable forever at
 * line 364, nothing logged, no crash report). Log the mod + its error chain first (cycle-safe), and if the cause
 * chain loops back on itself, hand Forge a flat copy so it reaches its normal crash screen instead of spinning.
 */
@Mixin(value = LoadController.class, remap = false)
public abstract class MixinLoadControllerErrorLog {
    @ModifyVariable(method = "errorOccurred", at = @At("HEAD"), argsOnly = true, remap = false)
    private Throwable dpp$logAndUnloop(Throwable t, ModContainer mod) {
        if (t == null) return null;
        StringBuilder sb = new StringBuilder("[PridePatches] Mod error from ")
                .append(mod == null ? "?" : mod.getModId()).append(':');
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        boolean loop = false;
        for (Throwable x = t; x != null; x = x.getCause()) {
            if (!seen.add(x)) { loop = true; sb.append("\n  (cause chain loops back here)"); break; }
            sb.append("\n  ").append(x);
            StackTraceElement[] st = x.getStackTrace();
            for (int i = 0; i < Math.min(12, st.length); i++) sb.append("\n      at ").append(st[i]);
            if (seen.size() > 30) { loop = true; break; }
        }
        LogManager.getLogger("PridePatches").error(sb.toString());
        if (!loop) return t;
        RuntimeException flat = new RuntimeException(t.toString() + " (original cause chain looped)");
        flat.setStackTrace(t.getStackTrace());
        return flat;
    }
}
