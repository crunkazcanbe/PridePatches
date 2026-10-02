package com.dogpound.patches.mixin;

import noppes.npcs.controllers.ScriptContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Custom NPCs world-start guard. ScriptController.load -> loadClientScripts ->
 * ClientScriptData.clear() does `this.script.clear()` but `script` is null before
 * it's lazily created -> NPE in the integrated-server tick (setAboutToStart) ->
 * world won't start. clear() on a non-existent script is a no-op, so skip it.
 * remap=false; target + field type resolved via compile-only stubs (excluded from jar).
 */
@Mixin(targets = "noppes.npcs.controllers.data.ClientScriptData", remap = false)
public abstract class MixinNpcsClientScriptData {
    @Shadow public ScriptContainer script;

    @Inject(method = "clear()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$skipClearWhenNoScript(CallbackInfo ci) {
        if (this.script == null) {
            ci.cancel();
        }
    }
}
