package com.dogpound.patches.mixin;

import com.dogpound.patches.AnomalyWells;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Thaumic Attempts re-scans every block in every entity's collision box looking for an anomaly well —
 * 11% of the server thread in the 2026-09-25 JFR profile, with zero wells in the world.
 * Skip the scan unless a well is actually loaded in that world.
 */
@Mixin(targets = "therealpant.thaumicattempts.events.AnomalyWellCollisionHandler", remap = false)
public abstract class MixinAnomalyWellCollision {
    @Inject(method = "onGetCollisionBoxes", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dpp$skipWithoutWells(GetCollisionBoxesEvent event, CallbackInfo ci) {
        if (!AnomalyWells.anyIn(event.getWorld())) ci.cancel();
    }
}
