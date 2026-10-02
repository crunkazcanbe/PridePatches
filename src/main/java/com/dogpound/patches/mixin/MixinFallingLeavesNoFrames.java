package com.dogpound.patches.mixin;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * LoliASM's onDemandAnimatedTextures (the FPS fix: stop re-uploading every animated texture every frame) drops
 * frame data it doesn't need, so a leaf sprite can report zero frames. Falling Leaves reads frame 0 to colour
 * its particles and crashed (crash-2026-09-25_20.14.04). No frames -> answer what it answers for "no pixels": white.
 */
@Mixin(targets = "com.xy.fallingleaves.util.LeafUtil", remap = false)
public abstract class MixinFallingLeavesNoFrames {
    @Inject(method = "averageColor", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dpp$noFrames(TextureAtlasSprite sprite, CallbackInfoReturnable<double[]> cir) {
        if (sprite.getFrameCount() == 0) cir.setReturnValue(new double[]{1.0, 1.0, 1.0});
    }
}
