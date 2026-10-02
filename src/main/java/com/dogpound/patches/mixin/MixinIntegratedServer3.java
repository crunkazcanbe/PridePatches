package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * World-join race guard. IntegratedServer's anonymous task #3 (IntegratedServer$3.run) runs on the
 * server thread during world load and calls mc.player.getUniqueID() — but mc.player can still be
 * null before the client player has finished spawning -> NPE -> "Error executing task" crash. It's
 * a timing dice-roll each world load (same family as the sound/entity packet races already guarded
 * in MixinWorldClient). Skip the task when there's no player yet; the player is registered normally
 * through the join flow once it exists, so nothing is lost. Vanilla target, remap=false, no refmap.
 */
@Mixin(targets = "net.minecraft.server.integrated.IntegratedServer$3", remap = false)
public abstract class MixinIntegratedServer3 {
    @Inject(method = "run()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$skipWhenNoPlayer(CallbackInfo ci) {
        if (Minecraft.getMinecraft().player == null) {
            ci.cancel();
        }
    }
}
