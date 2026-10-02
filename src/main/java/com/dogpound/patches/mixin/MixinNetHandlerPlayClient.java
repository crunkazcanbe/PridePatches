package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketSetSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla join-race guard. The server sends inventory SetSlot packets during join; with
 * this pack's altered load timing they can be processed before mc.player is set, so
 * NetHandlerPlayClient.handleSetSlot reads entityplayer.openContainer on a null player
 * -> NPE -> disconnect -> world teardown (which then drags DistantHorizons down too).
 * Skip the slot update until the player exists; the server re-syncs inventory anyway.
 */
@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {
    @Inject(method = "func_147266_a(Lnet/minecraft/network/play/server/SPacketSetSlot;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$skipSetSlotWhenNoPlayer(SPacketSetSlot packet, CallbackInfo ci) {
        if (Minecraft.getMinecraft().player == null) {
            ci.cancel();
        }
    }
}
