package com.dogpound.patches.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * RealWorld's cave moss/calcite code (3 methods) walks down to the floor with `while (world.isAirBlock(pos.down())) pos = pos.down();`.
 * Below y=0 the world reports AIR forever, so a column with nothing solid under it loops through billions of
 * positions — a brand-new world sat at "Preparing spawn area: 5%" for many minutes with 100% of the server thread
 * here (profiled 2026-10-02). Nothing at or below y=0 counts as air, so the walk stops at the world's bottom.
 */
@Mixin(targets = "realworld.worldgen.WorldgenCave", remap = false)
public abstract class MixinRealWorldMossFloor {
    @Redirect(method = {"spawnMossCluster", "spawnMossUnderStalactite", "spawnCalciteBlockUnderStalactite"}, require = 0,
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;func_175623_d(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean pride$notBelowWorld(World world, BlockPos pos) {
        return pos.getY() > 0 && world.isAirBlock(pos);
    }
}
