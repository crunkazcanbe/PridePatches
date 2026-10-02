package com.dogpound.patches.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Better Weather checks every loaded chunk each tick for lightning, looking at a square of positions that reaches into
 * NEIGHBOURING chunks. getRainHeight() fetches the chunk with world.getChunkFromChunkCoords, which LOADS and GENERATES
 * it when it isn't loaded — so chunks at the edge of the loaded area made the world generate (and run every mod's
 * world generator) outward every tick: ~1/3 of the server thread in a fresh world (profiled 2026-10-02).
 * On the server, an unloaded position now reports no rain height instead of being generated.
 */
@Mixin(value = paulevs.betterweather.api.WeatherAPI.class, remap = false)
public abstract class MixinBetterWeatherNoChunkGen {
    @Inject(method = "getRainHeight", at = @At("HEAD"), cancellable = true, require = 0)
    private static void pride$noGenerate(World world, int x, int z, CallbackInfoReturnable<Integer> cir) {
        if (world != null && !world.isRemote && !world.isBlockLoaded(new BlockPos(x, 64, z))) cir.setReturnValue(0);
    }

}
