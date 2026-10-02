package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GregTech Food Option's chat handler (ClientProxy.removeOtherPlayerMessages) filters
 * incoming chat by the player's potion effects — but a server chat/system message can
 * arrive while the world is still loading, before mc.player exists -> NPE on
 * mc.player.isPotionActive -> client task dies -> world teardown. Skip the filter when
 * there's no player yet (nothing to filter). remap=false; stub-resolved target.
 */
@Mixin(targets = "gregtechfoodoption.ClientProxy", remap = false)
public abstract class MixinGtfoClientProxy {
    @Inject(method = "removeOtherPlayerMessages(Lnet/minecraftforge/client/event/ClientChatReceivedEvent;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void dpp$skipChatFilterWhenNoPlayer(ClientChatReceivedEvent event, CallbackInfo ci) {
        if (Minecraft.getMinecraft().player == null) {
            ci.cancel();
        }
    }
}
