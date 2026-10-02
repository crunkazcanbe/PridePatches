package com.dogpound.patches.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Better Weather's lightning-rod check reads the block at a spot that may be in an unloaded chunk, which would
 *  generate it (see MixinBetterWeatherNoChunkGen). On the server, unloaded = no rod. */
@Mixin(targets = "paulevs.betterweather.util.LightningUtil", remap = false)
public abstract class MixinBetterWeatherRodNoChunkGen {
    @Inject(method = "isLightningRod", at = @At("HEAD"), cancellable = true, require = 0)
    private static void pride$noGenerate(World world, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (world != null && !world.isRemote && !world.isBlockLoaded(new BlockPos(x, y, z))) cir.setReturnValue(false);
    }
}
