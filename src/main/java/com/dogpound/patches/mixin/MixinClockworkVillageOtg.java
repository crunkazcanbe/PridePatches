package com.dogpound.patches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraftforge.event.terraingen.InitMapGenEvent;

/**
 * Clockwork Phase 2 swaps in its own village generator by casting the world's village generator to vanilla
 * vanilla classes (village, mineshaft, temple, stronghold). OTG worlds (Dregora…) use their own generators, so creating an OTG world crashed with a
 * ClassCastException. Leave non-vanilla village generators alone.
 */
@Pseudo
@Mixin(targets = "lumaceon.mods.clockworkphase2.handler.WorldGenHandler", remap = false)
public abstract class MixinClockworkVillageOtg {
    @Inject(method = "onMapGenInitialization(Lnet/minecraftforge/event/terraingen/InitMapGenEvent;)V", at = @At("HEAD"), cancellable = true)
    private void pride$skipNonVanillaVillages(InitMapGenEvent e, CallbackInfo ci) {
        // it casts villages, mineshafts, scattered features and strongholds to the vanilla classes; OTG replaces all of them
        Object gen = e.getNewGen();
        if (gen != null && !gen.getClass().getName().startsWith("net.minecraft.") && e.getType() != InitMapGenEvent.EventType.NETHER_BRIDGE
                && e.getType() != InitMapGenEvent.EventType.NETHER_CAVE) ci.cancel();
    }
}
