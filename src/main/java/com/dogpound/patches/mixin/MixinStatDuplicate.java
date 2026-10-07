package com.dogpound.patches.mixin;

import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Two mods with a mob of the same display name ("ghost": the MCreator Minecraft Dungeons mod + another) made
 * the game crash at start with "Duplicate stat id: stat.entityKill.ghost" (crash-2026-09-30_23.04.18), which is
 * why the Dungeons mod had been switched off. A duplicate kill/death STAT is harmless — skip registering the
 * second one (both mobs still exist and work; they just share one statistics line) instead of crashing.
 */
@Mixin(StatBase.class)
public abstract class MixinStatDuplicate {
    // registerStat() -> func_75971_g ; StatBase.statId -> field_75975_e ; StatList.getOneShotStat(String) -> func_151177_a
    @Inject(method = "func_75971_g()Lnet/minecraft/stats/StatBase;", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$skipDuplicate(CallbackInfoReturnable<StatBase> cir) {
        StatBase self = (StatBase) (Object) this;
        if (StatList.getOneShotStat(self.statId) != null) {
            System.out.println("[DogPoundPatches] duplicate stat id skipped (no crash): " + self.statId);
            cir.setReturnValue(self);
        }
    }
}
