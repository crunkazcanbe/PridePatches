package com.dogpound.patches.mixin;

import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * ProjectE's and Ancient Warfare's data walkers build {@code new ResourceLocation(nbt "id")} for every block entity the
 * data fixer touches (each structure, each chunk); with Hydrogen + StellarCore hooking that constructor they stayed in
 * the new-world profile after MixinFilteredFastId (2026-10-04). Hand them one shared instance per id instead.
 */
@Pseudo
@Mixin(targets = {"moze_intel.projecte.fixes.CapInventoryWalker",
                  "net.shadowmage.ancientwarfare.automation.datafixes.ItemMapDataWalker"}, remap = false)
public abstract class MixinWalkerSharedIds {
    @Redirect(method = "func_188266_a", require = 0,
              at = @At(value = "NEW", target = "net/minecraft/util/ResourceLocation"))
    private ResourceLocation pride$sharedId(String id) {
        return com.dogpound.patches.RlCache.of(id);
    }
}
