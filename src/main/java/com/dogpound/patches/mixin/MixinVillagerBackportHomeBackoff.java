package com.dogpound.patches.mixin;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Villager Backport re-runs up to 6 full path searches for a home every 5 s for EVERY homeless villager - including MCA
 * villagers (they extend EntityVillager but MCA manages their homes itself). In a busy village that was ~40% of the server
 * thread (profile 2026-10-03, lag around her lava building). MCA villagers are skipped, and a villager whose search found
 * nothing reachable waits 60 s before trying again.
 */
@Mixin(targets = "com.exiledradio.villagerbackport.home.HomeClaims", remap = false)
public abstract class MixinVillagerBackportHomeBackoff {
    private static final Map<EntityVillager, Long> PRIDE$RETRY_AT = new WeakHashMap<>();

    @Inject(method = "claimNearby", at = @At("HEAD"), cancellable = true, require = 0)
    private static void pride$skip(EntityVillager v, CallbackInfoReturnable<BlockPos> cir) {
        if (v.getClass().getName().startsWith("mca.")) { cir.setReturnValue(null); return; }
        Long at = PRIDE$RETRY_AT.get(v);
        if (at != null && v.world.getTotalWorldTime() < at) cir.setReturnValue(null);
    }

    @Inject(method = "claimNearby", at = @At("RETURN"), require = 0)
    private static void pride$backoff(EntityVillager v, CallbackInfoReturnable<BlockPos> cir) {
        if (cir.getReturnValue() == null) PRIDE$RETRY_AT.put(v, v.world.getTotalWorldTime() + 1200L);
        else PRIDE$RETRY_AT.remove(v);
    }
}
