package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Load-frame HUD guard. During world load the client game loop can render the
 * in-game HUD ONE frame before the player entity exists. GuiIngameForge's
 * renderGameOverlay (func_175180_a) — reached here via StellarCore's HUD caching
 * redirect — then dereferences mc.player (getRidingEntity / getVehicle) → NPE →
 * the client RENDER thread dies → the world appears "frozen" while the server
 * thread keeps spinning (high CPU, stuck screen).
 *
 * Skip the HUD for that one frame when there's no player yet. The loading screen
 * is a separate GuiScreen so it still draws; once the player spawns the HUD
 * renders normally. SRG name + remap=false to match the Cleanroom runtime.
 */
@Mixin(GuiIngameForge.class)
public abstract class MixinGuiIngameForge {

    @Inject(method = "func_175180_a(F)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$skipHudWhenNoPlayer(float partialTicks, CallbackInfo ci) {
        if (Minecraft.getMinecraft().player == null) {
            ci.cancel();
        }
    }
}
