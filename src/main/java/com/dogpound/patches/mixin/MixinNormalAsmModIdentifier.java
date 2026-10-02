package com.dogpound.patches.mixin;

import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * normalasm's crash-report mod-identifier calls LaunchClassLoader.untransformName via
 * reflection — but that method doesn't exist on Cleanroom's classloader, so it throws
 * NoSuchMethodException WHILE BUILDING A CRASH REPORT, scrambling the report and hiding
 * the real cause of every crash. Short-circuit it: just return the name unchanged so the
 * crash report completes and names the actual culprit. remap=false; stub-resolved target.
 */
@Mixin(targets = "mirror.normalasm.common.crashes.ModIdentifier", remap = false)
public abstract class MixinNormalAsmModIdentifier {
    @Inject(method = "untransformName(Lnet/minecraft/launchwrapper/LaunchClassLoader;Ljava/lang/String;)Ljava/lang/String;",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void dpp$noUntransform(LaunchClassLoader loader, String name, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(name);
    }
}
