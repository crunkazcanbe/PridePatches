package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Belt-and-suspenders companion to {@link MixinLeafiaPassiveLocal}: the actual
 * NPE site is LeafiaClientListener$Digamma.update(World) line 241, which reads
 * mc.player.posX. Guard the crash point directly too, in case update() is reached
 * from a path other than LeafiaPassiveLocal.onTick (e.g. an overlay render event).
 * remap=false: real (non-SRG) inner-class + method name.
 */
@Mixin(targets = "com.leafia.eventbuses.LeafiaClientListener$Digamma", remap = false)
public abstract class MixinLeafiaDigamma {

    @Inject(method = "update(Lnet/minecraft/world/World;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dpp$skipUpdateWhenNoPlayer(World world, CallbackInfo ci) {
        if (Minecraft.getMinecraft().player == null) {
            ci.cancel();
        }
    }
}
