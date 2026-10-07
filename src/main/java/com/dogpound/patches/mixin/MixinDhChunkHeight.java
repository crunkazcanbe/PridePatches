package com.dogpound.patches.mixin;

import com.dogpound.patches.DepthsHeight;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Distant Horizons 1.12.2 hard-codes chunks to Y 0..256 and reads storage slot i as Y i*16. With Depths Update the world is
 * Y -64..320 and slots 16..23 hold the sections above 256 and below 0, so DH never saw the deep layers and took the
 * deepslate slots for sky-high sections (LODs floating in the sky, 2026-10-03). Use the real range and slot heights.
 */
@Mixin(targets = "com.seibel.distanthorizons.common.wrappers.chunk.ChunkWrapper", remap = false)
public abstract class MixinDhChunkHeight {
    @Shadow @Final private Chunk chunk;

    @Inject(method = "getInclusiveMinBuildHeight(Lnet/minecraft/world/chunk/Chunk;)I", at = @At("HEAD"), cancellable = true, require = 0)
    private static void pride$min(Chunk c, CallbackInfoReturnable<Integer> cir) {
        if (DepthsHeight.on(c.getWorld())) cir.setReturnValue(DepthsHeight.minY(c.getWorld()));
    }

    @Inject(method = "getExclusiveMaxBuildHeight(Lnet/minecraft/world/chunk/Chunk;)I", at = @At("HEAD"), cancellable = true, require = 0)
    private static void pride$max(Chunk c, CallbackInfoReturnable<Integer> cir) {
        if (DepthsHeight.on(c.getWorld())) cir.setReturnValue(DepthsHeight.maxY(c.getWorld()));
    }

    @Inject(method = "getHeight(Lnet/minecraft/world/chunk/Chunk;)I", at = @At("HEAD"), cancellable = true, require = 0)
    private static void pride$height(Chunk c, CallbackInfoReturnable<Integer> cir) {
        World w = c.getWorld();
        if (DepthsHeight.on(w)) cir.setReturnValue(DepthsHeight.maxY(w) - DepthsHeight.minY(w) - 1); // DH: 255 for 0..256
    }

    @Inject(method = "getMinNonEmptyHeight()I", at = @At("HEAD"), cancellable = true, require = 0)
    private void pride$minNonEmpty(CallbackInfoReturnable<Integer> cir) {
        World w = chunk.getWorld();
        if (!DepthsHeight.on(w)) return;
        int min = DepthsHeight.maxY(w);
        ExtendedBlockStorage[] s = chunk.getBlockStorageArray();
        for (int i = 0; i < s.length; i++) if (s[i] != null && !s[i].isEmpty()) min = Math.min(min, DepthsHeight.sectionY(w, i));
        cir.setReturnValue(min == DepthsHeight.maxY(w) ? DepthsHeight.minY(w) : min);
    }

    @Inject(method = "getMaxNonEmptyHeight()I", at = @At("HEAD"), cancellable = true, require = 0)
    private void pride$maxNonEmpty(CallbackInfoReturnable<Integer> cir) {
        World w = chunk.getWorld();
        if (!DepthsHeight.on(w)) return;
        int max = DepthsHeight.minY(w);
        ExtendedBlockStorage[] s = chunk.getBlockStorageArray();
        for (int i = 0; i < s.length; i++) if (s[i] != null && !s[i].isEmpty()) max = Math.max(max, DepthsHeight.sectionY(w, i) + 16);
        cir.setReturnValue(Math.min(max, DepthsHeight.maxY(w)));
    }
}
