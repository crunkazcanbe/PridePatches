package com.dogpound.patches.mixin;

import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MoreCreeps Revival's castle steps DOWN from y=200 until 3 of its 4 corners are solid. One corner is 30 blocks
 * out, in a chunk that isn't generated yet; our cascade guard answers "air" there, so the loop never ended and a
 * new flat world hung forever (10-03, 2 billion reads). The guard now answers bedrock at y<=0, which stops the loop
 * — and a castle that only "found ground" down there is skipped instead of being built at the bottom of the world.
 */
@Mixin(targets = "com.morecreepsrevival.morecreeps.common.world.WorldGenCastle", remap = false)
public abstract class MixinMoreCreepsCastleFloor {
    @Inject(method = "findStructurePos", at = @At("RETURN"), cancellable = true, require = 0)
    private void pride$noBedrockCastle(CallbackInfoReturnable<BlockPos> cir) {
        BlockPos p = cir.getReturnValue();
        if (p != null && p.getY() < 8) cir.setReturnValue(null);
    }
}
