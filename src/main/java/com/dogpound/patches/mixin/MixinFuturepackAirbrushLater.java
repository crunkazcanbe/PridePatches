package com.dogpound.patches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dynamic Surroundings crash (her pack, 2026-09-28): Futurepack's "Airbush Recipe Scanner" thread starts in the
 * middle of loading and builds a fake WorldClient; that fires WorldEvent.Load, Dynamic Surroundings answers it before
 * its biome registry exists (RegistryManager.BIOME == null) and its EnvironState class dies for good →
 * "Could not initialize class EnvironStateHandler$EnvironState" at load-complete. A race, so it came and went.
 * Hold the scanner here; ClientLoadedHooks starts it once the game has finished loading.
 */
@Pseudo
@Mixin(targets = "futurepack.common.crafting.FPAirBrushRecipes", remap = false)
public abstract class MixinFuturepackAirbrushLater {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dpp$later(CallbackInfo ci) {
        try {
            Object instance = Class.forName("futurepack.common.crafting.FPAirBrushRecipes").getField("instance").get(null);
            if (instance instanceof Runnable) {
                com.dogpound.patches.client.ClientLoadedHooks.later("Airbush Recipe Scanner", (Runnable) instance);
                ci.cancel();
            }
        } catch (Throwable ignored) {}                                         // can't find it: let Futurepack start it as before
    }
}
