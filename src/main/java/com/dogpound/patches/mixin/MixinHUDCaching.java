package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * StellarCore HUD-caching renders the in-game overlay (crosshair, etc.) directly via
 * renderCachedHud during world load — but the parent RenderGameOverlayEvent (set by
 * GuiIngameForge.renderGameOverlay) isn't there yet, so pre() builds a sub-event with
 * a null parent -> NPE -> freeze. Skip the cached HUD entirely while mc.player is null;
 * there's nothing to draw before the player spawns. remap=false; stub-resolved target.
 */
@Mixin(targets = "github.kasuminova.stellarcore.client.hudcaching.HUDCaching", remap = false)
public abstract class MixinHUDCaching {
    @Inject(method = "renderCachedHud(Lnet/minecraft/client/renderer/EntityRenderer;Lnet/minecraft/client/gui/GuiIngame;F)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void dpp$skipCachedHudWhenNoPlayer(EntityRenderer er, GuiIngame gi, float f, CallbackInfo ci) {
        if (Minecraft.getMinecraft().player == null) {
            ci.cancel();
        }
    }
}
