package com.dogpound.patches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PolyPatcher's "unfocused sound volume": on start it asks Display.isActive(); vanilla Forge says false there, but
 * Cleanroom says TRUE, so it runs its "focus came back" branch and restores a volume it never saved (-1) → Minecraft's
 * MASTER volume became -1 = every click, pop, hover and game sound silent until the window lost and regained focus
 * (her 2026-10-05: "it was working four boots ago"). Only restore a volume that was really saved.
 */
@Mixin(targets = "club.sk1er.patcher.util.world.sound.SoundHandler", remap = false)
public abstract class MixinPatcherFocusVolume {
    @Shadow private boolean previousActive;
    @Shadow private float previousVolume;

    @Inject(method = "handleFocusChange", at = @At("HEAD"), cancellable = true, require = 0)
    private void pridepatches$noRestoreOfUnsaved(CallbackInfo ci) {
        if (previousActive && previousVolume < 0f) ci.cancel();
    }
}
