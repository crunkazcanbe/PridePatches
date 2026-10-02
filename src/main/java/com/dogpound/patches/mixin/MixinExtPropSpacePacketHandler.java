package com.dogpound.patches.mixin;

import com.hbmspace.packet.toclient.ExtPropSpacePacket;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NTM-Space syncs space-radiation props on world join via ExtPropSpacePacket. Its client
 * apply-task checks mc.world but NOT mc.player, so when a sync packet arrives before the
 * player spawns it calls HbmLivingPropsSpace.getData(mc.player==null) -> NPE -> client
 * task dies -> world teardown. Skip applying the packet until the player exists (it'll be
 * re-synced). remap=false; stub-resolved target + lambda method.
 */
@Mixin(targets = "com.hbmspace.packet.toclient.ExtPropSpacePacket$Handler", remap = false)
public abstract class MixinExtPropSpacePacketHandler {
    @Inject(method = "lambda$onMessage$0(Lcom/hbmspace/packet/toclient/ExtPropSpacePacket;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void dpp$skipApplyWhenNoPlayer(ExtPropSpacePacket packet, CallbackInfo ci) {
        if (Minecraft.getMinecraft().player == null) {
            ci.cancel();
        }
    }
}
