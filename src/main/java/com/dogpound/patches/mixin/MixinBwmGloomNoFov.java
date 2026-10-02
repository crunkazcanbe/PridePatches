package com.dogpound.patches.mixin;

import net.minecraftforge.client.event.FOVUpdateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Better With Mods' Gloom also warps the field of view while you're in the dark; keep the gloom itself, drop the warp. */
@Mixin(targets = "betterwithmods.module.hardcore.needs.HCGloom", remap = false)
public abstract class MixinBwmGloomNoFov {
    @Inject(method = "onFOVUpdate", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$keepFov(FOVUpdateEvent event, CallbackInfo ci) { ci.cancel(); }
}
