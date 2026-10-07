package com.dogpound.patches.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * GeoCraft walks its loaded-atmosphere map while the same loop loads and queues unloads of atmosphere data, which
 * changes that map: fastutil's iterator then dies with "this.wrapped is null" and the server crashes. Happened in
 * Chunk Pregenerator's world Preview (many chunks loading/unloading at once), 2026-10-04 12:51. Walk a copy instead.
 */
@Pseudo
@Mixin(targets = "top.qiguaiaaaa.geocraft.geography.atmosphere.system.QiguaiAtmosphereSystem", remap = false)
public abstract class MixinGeoCraftAtmosphereSnapshot {
    @Redirect(method = "updateAtmospheres", require = 0,
              at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    private Iterator<?> pride$snapshot(Collection<?> loaded) {
        return new ArrayList<Object>(loaded).iterator();
    }
}
