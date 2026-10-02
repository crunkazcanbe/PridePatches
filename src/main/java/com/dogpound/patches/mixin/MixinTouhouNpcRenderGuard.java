package com.dogpound.patches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Touhou Little Maid's Custom NPCs compat builds a renderer from noppes.npcs.client.layer.LayerHeadwear, which only
 * the OFFICIAL Custom NPCs has — the pack's Unofficial CNPC doesn't, and postInit died. Skip that compat when the
 * class is missing; maids themselves are unaffected.
 */
@Mixin(targets = "com.github.tartaricacid.touhoulittlemaid.proxy.ClientProxy", remap = false)
public abstract class MixinTouhouNpcRenderGuard {
    @Inject(method = "changeNpcRender", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$onlyWithOfficialNpcs(CallbackInfo ci) {
        try { Class.forName("noppes.npcs.client.layer.LayerHeadwear", false, getClass().getClassLoader()); }
        catch (Throwable missing) { System.out.println("[PridePatches] Touhou Little Maid: Custom NPCs maid-render compat skipped (LayerHeadwear missing in this CNPC)"); ci.cancel(); }
    }
}
