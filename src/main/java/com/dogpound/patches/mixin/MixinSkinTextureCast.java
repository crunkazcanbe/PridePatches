package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla getDownloadImageSkin casts whatever texture sits at "skins/<name>" to ThreadDownloadImageData. Another mod had
 * already put a plain SimpleTexture there, so opening the spectator teleport menu (number key in spectator mode) crashed
 * the game: crash-2026-10-03_14.35.13 ClassCastException SimpleTexture -> ThreadDownloadImageData. Now the caller gets an
 * unregistered placeholder instead; the texture that's already there is left alone and keeps being drawn.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class MixinSkinTextureCast {
    @Inject(method = "func_110304_a(Lnet/minecraft/util/ResourceLocation;Ljava/lang/String;)Lnet/minecraft/client/renderer/ThreadDownloadImageData;",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void pride$noBadSkinCast(ResourceLocation rl, String name, CallbackInfoReturnable<ThreadDownloadImageData> cir) {
        ITextureObject tex = Minecraft.getMinecraft().getTextureManager().getTexture(rl);
        if (tex != null && !(tex instanceof ThreadDownloadImageData)) cir.setReturnValue(new ThreadDownloadImageData(null, null, rl, null));
    }
}
