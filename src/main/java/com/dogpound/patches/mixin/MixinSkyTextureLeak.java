package com.dogpound.patches.mixin;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Some mod turns GL_TEXTURE_2D on with raw GL11 behind GlStateManager's back, so the cache still says "off" and
 * renderSky's disableTexture2D() does nothing: the flat sky plane got painted with whatever texture was bound last
 * (the block atlas = "upside-down world in the sky", a vehicle sheet = "cars in the sky", 2026-10-03).
 * Force texturing really OFF (GL and the cache) on texture unit 0 before the sky draws; vanilla re-enables it
 * through GlStateManager for the sun and moon, which now works because cache and GL agree.
 */
@Mixin(value = RenderGlobal.class, remap = false)
public abstract class MixinSkyTextureLeak {
    @Inject(method = "func_174976_a(FI)V", at = @At("HEAD"), require = 0)
    private void pride$noSkyTexture(float partialTicks, int pass, CallbackInfo ci) {
        GlStateManager.setActiveTexture(net.minecraft.client.renderer.OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();   // cache -> on (no-op if it already thinks so) ...
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GlStateManager.disableTexture2D();  // ... then really off, cache and GL in agreement
    }
}
