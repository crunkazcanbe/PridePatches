package com.dogpound.patches.mixin;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sibling of {@link MixinExtPropSpacePacketHandler}: base NTM's HbmPlayerSyncPacket apply
 * task null-checks mc.world but NOT mc.player, then calls HbmLivingProps.getData(player)
 * -> NPE when a sync packet lands before the player spawns on join. Skip until the player
 * exists (it gets re-synced). remap=false; stub-resolved target + lambda.
 */
@Mixin(targets = "com.hbm.packet.toclient.HbmPlayerSyncPacket$Handler", remap = false)
public abstract class MixinHbmPlayerSyncPacketHandler {
    @Inject(method = "lambda$onMessage$0(Lio/netty/buffer/ByteBuf;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void dpp$skipApplyWhenNoPlayer(ByteBuf buf, CallbackInfo ci) {
        if (Minecraft.getMinecraft().player == null) {
            ci.cancel();
        }
    }
}
