package com.dogpound.patches.mixin;

import net.minecraft.world.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge throws "Attempted to modify LootTable after being finalized!" when a mod adds/removes a pool on a
 * loot table that is already frozen. With 450+ mods some LootTableLoadEvent handler always does it
 * (The Midnight's FishingLoot did, 2026-09-25) and it kills world creation in the server tick loop.
 * Log it once per table and carry on instead: the pool change is simply ignored.
 */
@Mixin(value = LootTable.class, remap = false)
public abstract class MixinLootTableFrozen {

    @Shadow(remap = false) public abstract boolean isFrozen();

    @Inject(method = "checkFrozen", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$dontCrashOnFrozen(CallbackInfo ci) {
        if (isFrozen()) {
            System.out.println("[DogPoundPatches] a mod tried to change a finalized loot table — ignored instead of crashing");
            ci.cancel();
        }
    }
}
