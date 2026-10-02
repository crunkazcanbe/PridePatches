package com.dogpound.patches.mixin;

import io.netty.channel.ChannelFuture;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * "Save and Quit to Title" froze forever (2026-09-29): NetworkManager.closeChannel does
 * channel.close().awaitUninterruptibly() with NO timeout, and on Cleanroom's Netty the local (single-player)
 * channel's close future never completes — all Netty threads idle, client thread parked for good.
 * The world is already saved by then; the quit just never reaches loadWorld(null).
 * Wait at most 3 s, then carry on with the normal quit (server shutdown + final save still run).
 */
@Mixin(NetworkManager.class)
public abstract class MixinNetworkManagerCloseTimeout {
    @Redirect(method = "func_150718_a(Lnet/minecraft/util/text/ITextComponent;)V",
              at = @At(value = "INVOKE",
                       target = "Lio/netty/channel/ChannelFuture;awaitUninterruptibly()Lio/netty/channel/ChannelFuture;"),
              remap = false)
    private ChannelFuture dpp$closeWithTimeout(ChannelFuture future) {
        if (!future.awaitUninterruptibly(3000L)) {
            System.out.println("[PridePatches] network close did not finish in 3s - continuing the quit anyway");
        }
        return future;
    }
}
