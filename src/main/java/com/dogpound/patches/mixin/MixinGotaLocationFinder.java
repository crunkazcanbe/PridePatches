package com.dogpound.patches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Gates of the Apocalypse picks its fortress spot on world load by testing up to 1000 random
 * places within +-1500 blocks of 0,0. Every test force-generates fresh chunks, and in this pack each
 * chunk runs hundreds of mods' worldgen, so loading a new world sat for 20+ minutes (2026-10-03:
 * heap at 89%, server thread deep in chunk population under LocationFinder). Search 16 spots within
 * +-256 blocks instead: same fortress, near spawn, a few dozen chunks instead of thousands.
 */
@Mixin(targets = "com.apocalypsesurvivor.gatesoftheapocalypse.structure.LocationFinder", remap = false)
public abstract class MixinGotaLocationFinder {
    @ModifyConstant(method = "findLocation", constant = @Constant(intValue = 1000), require = 0)
    private static int pride$fewerTries(int original) { return 16; }

    @ModifyConstant(method = "findLocation", constant = @Constant(intValue = 3001), require = 0)
    private static int pride$smallerSpan(int original) { return 513; }

    @ModifyConstant(method = "findLocation", constant = @Constant(intValue = 1500), require = 0)
    private static int pride$smallerRadius(int original) { return 256; }
}
