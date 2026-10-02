package com.dogpound.patches.mixin;

import net.minecraftforge.client.event.FOVUpdateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Better With Mods' hardcore movement changes walk speed per block (dirt, gravel, grass...) and ALSO rescales
 * the field of view every time, so walking across mixed ground makes the camera zoom in and out — "jumpy".
 * Keep the speed changes, drop the FOV jolt. (Her request 2026-09-25: keep BWM, fix the camera.)
 */
@Mixin(targets = "betterwithmods.module.hardcore.needs.HCMovement", remap = false)
public abstract class MixinBwmNoFovJolt {
    @Inject(method = "onFOV", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$keepFov(FOVUpdateEvent event, CallbackInfo ci) { ci.cancel(); }
}
