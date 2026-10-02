package com.dogpound.patches.mixin;

import com.dogpound.patches.HacBlockIndex;
import defeatedcrow.hac.api.climate.BlockSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Heat&Climate looks up every block's heat/humidity/airflow by walking the whole registered Set calling
 * equals() on each entry (equals has a 32767 meta wildcard, so hashing can't be used) — 6% of the server
 * thread in the 2026-09-25 JFR profile. Answer from a per-Block index instead.
 */
@Mixin(targets = "defeatedcrow.hac.core.climate.HeatBlockRegister", remap = false)
public abstract class MixinHeatBlockRegister {
    @Inject(method = "include", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$indexedInclude(Set<BlockSet> set, BlockSet key, CallbackInfoReturnable<BlockSet> cir) {
        if (key == null) return; // original handles it
        try { cir.setReturnValue(HacBlockIndex.find(set, key)); } catch (RuntimeException setChangedMidRebuild) { /* original loop */ }
    }
}
