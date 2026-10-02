package com.dogpound.patches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Custom NPCs' VersionChecker thread busy-waits `while (mc.player == null) {}` with no sleep — one CPU core pinned
 * from launch until you join a world (2026-10-01 JFR: 46% of ALL samples in the load), only to post a chat advert.
 * Skip it.
 */
@Mixin(targets = "noppes.npcs.client.VersionChecker", remap = false)
public abstract class MixinNpcsVersionCheckerSpin {
    @Inject(method = "run", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$noSpin(CallbackInfo ci) { ci.cancel(); }
}
