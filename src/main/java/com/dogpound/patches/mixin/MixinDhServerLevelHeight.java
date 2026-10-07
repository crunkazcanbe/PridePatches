package com.dogpound.patches.mixin;

import com.dogpound.patches.DepthsHeight;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** DH ServerLevelWrapper height range follows Depths Update (Y -64..320) instead of 0..256; see MixinDhChunkHeight. */
@Mixin(targets = "com.seibel.distanthorizons.common.wrappers.world.ServerLevelWrapper", remap = false)
public abstract class MixinDhServerLevelHeight {
    @Shadow @Final private WorldServer level;

    @Inject(method = "getMinHeight()I", at = @At("HEAD"), cancellable = true, require = 0)
    private void pride$min(CallbackInfoReturnable<Integer> cir) {
        if (DepthsHeight.on(level)) cir.setReturnValue(DepthsHeight.minY(level));
    }

    @Inject(method = "getMaxHeight()I", at = @At("HEAD"), cancellable = true, require = 0)
    private void pride$max(CallbackInfoReturnable<Integer> cir) {
        if (DepthsHeight.on(level)) cir.setReturnValue(DepthsHeight.maxY(level));
    }
}
