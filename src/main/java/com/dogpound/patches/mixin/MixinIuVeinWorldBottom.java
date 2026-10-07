package com.dogpound.patches.mixin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Industrial Upgrade's ore veins write straight into a chunk's storage sections at y>>4. Depths Update makes the world
 * deeper (24 sections, build height below 0 allowed), so a vein stepping down to find ground reached y<0 -> section
 * index -1 -> ArrayIndexOutOfBounds, and creating a world crashed (crash-2026-10-03_02.21.07, AlgorithmVein:602).
 * Such a block is now just not placed (IU's own caller already treats null as "nothing placed").
 */
@Mixin(targets = "com.denfop.world.vein.AlgorithmVein", remap = false)
public abstract class MixinIuVeinWorldBottom {
    @Inject(method = "setBlockState(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void pride$insideChunk(World world, Chunk chunk, BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> cir) {
        int section = pos.getY() >> 4;
        if (pos.getY() < 0 || section >= chunk.getBlockStorageArray().length) cir.setReturnValue(null);
    }
}
