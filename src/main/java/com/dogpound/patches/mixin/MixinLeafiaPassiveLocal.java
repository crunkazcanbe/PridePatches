package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Load-frame guard for NTM Cursed's Leafia client passive system.
 *
 * During world load the client tick fires (FMLCommonHandler.onPreClientTick ->
 * LeafiaClientListener.clientTick -> LeafiaPassiveLocal.onTick) ONE or more ticks
 * before the player entity exists. The passive effects (radiation / digamma /
 * vignette) read mc.player.posX (field_70165_t) → NPE → the client dies and the
 * world freezes at "Building terrain".
 *
 * Skip the whole client passive tick while there's no player yet; it's all
 * player-relative and has nothing to do until the player spawns. Once mc.player
 * is non-null it ticks normally. remap=false: real (non-SRG) class + method name.
 */
@Mixin(targets = "com.leafia.passive.LeafiaPassiveLocal", remap = false)
public abstract class MixinLeafiaPassiveLocal {

    @Inject(method = "onTick(Lnet/minecraft/world/World;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dpp$skipTickWhenNoPlayer(World world, CallbackInfo ci) {
        if (Minecraft.getMinecraft().player == null) {
            ci.cancel();
        }
    }
}
