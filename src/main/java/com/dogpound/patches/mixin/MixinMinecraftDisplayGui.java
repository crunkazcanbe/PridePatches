package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla join-race guard. Minecraft.displayGuiScreen, when a screen closes, checks
 * this.player.getHealth() <= 0 to decide whether to show the death screen. During join a
 * screen can close while mc.player is still null -> NPE -> "Updating screen events" crash
 * (e.g. pressing a key on the black pre-spawn screen). Treat a null player as full health
 * so it just shows the requested screen instead of crashing. remap=false (vanilla SRG).
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraftDisplayGui {
    @Redirect(method = "func_147108_a(Lnet/minecraft/client/gui/GuiScreen;)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;func_110143_aJ()F"),
              remap = false)
    private float dpp$safeHealth(EntityPlayerSP player) {
        return player == null ? 20.0F : player.getHealth();
    }
}
