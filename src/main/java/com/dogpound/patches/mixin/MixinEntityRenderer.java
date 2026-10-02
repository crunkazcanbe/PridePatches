package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Load-frame race guard (SURGICAL). When a world finishes loading with the saved player in an
 * HBM space/celestial dimension, the client can run a render frame BEFORE the view entity is
 * assigned. HBM's acid-rain overwrite of the world render (com.leafia.overwrite_contents
 * MixinEntityRenderer_AcidRain) then dereferences that null view entity (getSkyColor /
 * entity.getLook / camera pos), NPE-crashing the client.
 *
 * Target renderWorld (func_78471_a) — the WORLD GEOMETRY pass — NOT updateCameraAndRender. If
 * the render view entity is null, point it at the player; if the player isn't ready yet, cancel
 * ONLY this world pass. The caller (updateCameraAndRender) keeps going and still draws the GUI
 * and the custom "Building terrain" loading screen — so no crash AND no black loading screen.
 * SRG name + remap=false to match the Cleanroom runtime; body uses MCP names reobfJar converts.
 */
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Inject(method = "func_78471_a(FJ)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$skipWorldRenderWhenNoEntity(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getRenderViewEntity() == null) {
            if (mc.player != null) {
                mc.setRenderViewEntity(mc.player);
            } else {
                ci.cancel();   // skip ONLY world geometry; GUI + loading screen still draw
            }
        }
    }
}
