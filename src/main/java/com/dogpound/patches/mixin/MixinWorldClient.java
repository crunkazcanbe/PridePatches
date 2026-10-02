package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * THE shared chokepoint for join-time entity packets. WorldClient.getEntityByID does
 * `id == this.mc.player.getEntityId() ? mc.player : super.getEntityByID(id)`. During join
 * the server streams entity packets (head-look, move, velocity, metadata, spawn) that all
 * route through here BEFORE mc.player exists -> NPE on mc.player.getEntityId(). Make that
 * one comparison null-safe: if there's no player yet, return an id that can't match (-1)
 * so it falls through to the normal lookup. Fixes the entire entity-packet family at once,
 * instead of guarding each packet. remap=false (vanilla SRG names).
 */
@Mixin(WorldClient.class)
public abstract class MixinWorldClient {
    @Redirect(method = "func_73045_a(I)Lnet/minecraft/entity/Entity;",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;func_145782_y()I"),
              remap = false)
    private int dpp$safePlayerEntityId(EntityPlayerSP player) {
        return player == null ? -1 : player.getEntityId();
    }

    /**
     * Sibling of the bug above, for the SOUND-packet family. WorldClient.playSound(...)
     * computes `this.mc.getRenderViewEntity().getDistanceSq(x,y,z)` to position the sound.
     * During join the server can stream a sound packet BEFORE the render-view entity (the
     * player) exists -> getRenderViewEntity() is null -> NPE at WorldClient:514. A sound
     * can't be positioned with no listener in the world anyway, so just skip it when there's
     * no view entity yet. Cancels cleanly at HEAD. func_184134_a = the (DDD...Z) playSound.
     */
    @Inject(method = "func_184134_a(DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FFZ)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$skipSoundWhenNoViewEntity(double x, double y, double z, SoundEvent sound,
            SoundCategory category, float volume, float pitch, boolean distanceDelay, CallbackInfo ci) {
        if (Minecraft.getMinecraft().getRenderViewEntity() == null) {
            ci.cancel();
        }
    }
}
