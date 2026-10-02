package com.dogpound.patches.mixin;

import cam72cam.mod.net.Packet;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Immersive Railroading's UniversalModCore applies network packets via a client task that
 * calls MinecraftClient.getPlayer() — which THROWS "Called to get the player before
 * minecraft has actually started!" when a packet lands during join before the player
 * exists. Skip the apply until mc.player is ready (the packet re-syncs). remap=false;
 * stub-resolved target + lambda. (Client-only check; harmless on integrated server.)
 */
@Mixin(targets = "cam72cam.mod.net.Packet$Handler", remap = false)
public abstract class MixinUmcPacketHandler {
    @Inject(method = "lambda$onMessage$0(Lcam72cam/mod/net/Packet$Message;Lnet/minecraftforge/fml/common/network/simpleimpl/MessageContext;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$skipUmcPacketWhenNoPlayer(Packet.Message msg, MessageContext ctx, CallbackInfo ci) {
        if (Minecraft.getMinecraft().player == null) {
            ci.cancel();
        }
    }
}
