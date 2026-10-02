package com.dogpound.patches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * More MekaSuit Modules registers the MekaSuit helmet as an AE2 wireless terminal by casting it to
 * IWirelessTermHandler — only true in the Mekanism fork it was built for, so init crashed with a
 * ClassCastException. Skip just that registration; the rest of the mod loads.
 */
@Mixin(targets = "moremekasuitmodules.common.MoreMekaSuitModulesCommonProxy", remap = false)
public abstract class MixinMoreMekaSuitAE {
    @Inject(method = "AEregistries", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$skipAeHelmetTerminal(CallbackInfo ci) {
        ci.cancel(); // this pack's Mekanism helmet never implements IWirelessTermHandler
    }
}
