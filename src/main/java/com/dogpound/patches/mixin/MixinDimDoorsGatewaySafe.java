package com.dogpound.patches.mixin;

import net.minecraft.world.World;
import org.dimdev.ddutils.schem.Schematic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Dimensional Doors throws from Schematic.place when a gateway schematic's tile-entity data doesn't match the
 * block that actually landed there — during chunk population that kills the whole server tick loop
 * (crash-2026-09-25_20.38.07, via Mystcraft's background instability profiling). Skip that one gateway instead.
 */
@Mixin(targets = "org.dimdev.dimdoors.shared.world.gateways.BaseSchematicGateway", remap = false)
public abstract class MixinDimDoorsGatewaySafe {
    @Redirect(method = "generate", remap = false,
              at = @At(value = "INVOKE", target = "Lorg/dimdev/ddutils/schem/Schematic;place(Lnet/minecraft/world/World;III)V", remap = false))
    private void dpp$placeSafely(Schematic schematic, World world, int x, int y, int z) {
        try {
            schematic.place(world, x, y, z);
        } catch (RuntimeException e) {
            System.out.println("[PridePatches] skipped a broken Dimensional Doors gateway at " + x + "," + y + "," + z + ": " + e.getMessage());
        }
    }
}
