package com.dogpound.patches.mixin;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.IntCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Minecraft's Voronoi zoom only works when ((x - 2) & 3) + width fits its 4-block grid: vanilla always asks 16-aligned areas,
 * but BuildCraft's oil-biome replacer asks a shifted, wider one, and the zoom reads one past its scratch array:
 * "arraycopy: last source index 577 out of bounds for int[576]" (her Dregora crashes 2026-10-04, via HBM's biome check).
 * Such a request is answered from the enclosing grid-aligned area, cut to exactly what was asked: same biomes, no crash.
 */
@Mixin(value = GenLayerVoronoiZoom.class, remap = false)
public abstract class MixinVoronoiAligned {
    @Inject(method = "func_75904_a", at = @At("HEAD"), cancellable = true, remap = false)
    private void pride$aligned(int x, int z, int w, int h, CallbackInfoReturnable<int[]> cir) {
        // vanilla first shifts the area by -2, so the grid it needs is "x - 2 is a multiple of 4"
        boolean xOk = ((x - 2) & 3) + w <= (((w >> 2) + 1) << 2), zOk = ((z - 2) & 3) + h <= (((h >> 2) + 1) << 2);
        if (xOk && zOk) return;
        int ax = ((x - 2) & ~3) + 2, az = ((z - 2) & ~3) + 2;
        int aw = ((x - ax) + w + 3) & ~3, ah = ((z - az) + h + 3) & ~3;   // aligned start + multiple-of-4 size: always safe
        int[] big = ((GenLayer) (Object) this).getInts(ax, az, aw, ah);
        int[] out = IntCache.getIntCache(w * h);
        int dx = x - ax, dz = z - az;
        for (int row = 0; row < h; row++) System.arraycopy(big, (row + dz) * aw + dx, out, row * w, w);
        cir.setReturnValue(out);
    }
}
