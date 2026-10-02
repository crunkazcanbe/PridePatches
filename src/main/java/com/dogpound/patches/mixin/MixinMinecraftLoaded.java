package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The end of Minecraft.init — every mod has finished loading. Runs work other patches held back (ClientLoadedHooks). */
@Mixin(Minecraft.class)
public abstract class MixinMinecraftLoaded {
    @Inject(method = "func_71384_a()V", at = @At("TAIL"), remap = false)
    private void dpp$loaded(CallbackInfo ci) {
        com.dogpound.patches.client.ClientLoadedHooks.run();
    }
}
