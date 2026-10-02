package com.dogpound.patches.mixin;

import net.minecraftforge.event.world.ChunkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Traincraft's chunk-unload handler looks up Jukebox Carts in every unloading chunk to mute their MP3 player — a
 * client-only thing — but it ran on the SERVER too, for every chunk unload. In a fresh world's unload storms the server
 * thread sat in that lookup (profiled 2026-10-02). Server side: skip it (no MP3 players exist there).
 */
@Mixin(value = train.common.core.handlers.WorldEvents.class, remap = false)
public abstract class MixinTraincraftUnloadClientOnly {
    @Inject(method = "chunkUnloadEvent", at = @At("HEAD"), cancellable = true, require = 0)
    private void pride$clientOnly(ChunkEvent.Unload event, CallbackInfo ci) {
        if (event.getWorld() != null && !event.getWorld().isRemote) ci.cancel();
    }
}
