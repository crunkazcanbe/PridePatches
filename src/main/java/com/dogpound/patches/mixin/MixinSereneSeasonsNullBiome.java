package com.dogpound.patches.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Serene Seasons finds the World variable for its rain/snow/freeze hooks by counting instructions back from vanilla
 * code. Under Cleanroom that count can land on the wrong slot, so its helpers get a Biome where a World should be
 * (or the reverse). Java runs with bytecode verification off here, so that crashed the client while it rained:
 * an NPE in getRegistryName, then a SIGSEGV in canBlockFreezeInSeason (2026-10-04, pridedev).
 * Every helper now checks the real types first. Swapped World/Biome gets put back in the right order;
 * anything else wrong returns "no" (no extra snow, freezing or rain effect) and is logged once.
 */
@Pseudo
@Mixin(targets = "sereneseasons.season.SeasonASMHelper", remap = false)
public abstract class MixinSereneSeasonsNullBiome {
    @Inject(method = "shouldAddRainParticles", at = @At("HEAD"), cancellable = true)
    private static void pride$rainParticles(@Coerce Object a, @Coerce Object b, CallbackInfoReturnable<Boolean> cir) {
        pride$worldBiome(a, b, cir, "shouldAddRainParticles");
    }

    @Inject(method = "shouldRenderRainSnow", at = @At("HEAD"), cancellable = true)
    private static void pride$rainRender(@Coerce Object a, @Coerce Object b, CallbackInfoReturnable<Boolean> cir) {
        pride$worldBiome(a, b, cir, "shouldRenderRainSnow");
    }

    @Inject(method = "canBlockFreezeInSeason(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ZLsereneseasons/api/season/ISeasonState;Z)Z", at = @At("HEAD"), cancellable = true)
    private static void pride$freeze(@Coerce Object w, @Coerce Object pos, boolean water, @Coerce Object season, boolean tropical, CallbackInfoReturnable<Boolean> cir) {
        pride$worldPos(w, pos, cir, "canBlockFreezeInSeason");
    }

    @Inject(method = "canSnowAtInSeason(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ZLsereneseasons/api/season/ISeasonState;Z)Z", at = @At("HEAD"), cancellable = true)
    private static void pride$snow(@Coerce Object w, @Coerce Object pos, boolean light, @Coerce Object season, boolean tropical, CallbackInfoReturnable<Boolean> cir) {
        pride$worldPos(w, pos, cir, "canSnowAtInSeason");
    }

    @Inject(method = "isRainingAtInSeason", at = @At("HEAD"), cancellable = true)
    private static void pride$raining(@Coerce Object w, @Coerce Object pos, @Coerce Object season, CallbackInfoReturnable<Boolean> cir) {
        pride$worldPos(w, pos, cir, "isRainingAtInSeason");
    }

    @Inject(method = "getFloatTemperature(Lnet/minecraft/world/World;Lnet/minecraft/world/biome/Biome;Lnet/minecraft/util/math/BlockPos;)F", at = @At("HEAD"), cancellable = true)
    private static void pride$temperature(@Coerce Object w, @Coerce Object biome, @Coerce Object pos, CallbackInfoReturnable<Float> cir) {
        if (w instanceof World && ((World) w).provider != null && biome instanceof Biome && pos instanceof BlockPos) return;
        com.dogpound.patches.SereneSeasonsLog.once("getFloatTemperature", w, biome);
        Biome b = w instanceof Biome ? (Biome) w : biome instanceof Biome ? (Biome) biome : pos instanceof Biome ? (Biome) pos : null;
        BlockPos p = pos instanceof BlockPos ? (BlockPos) pos : w instanceof BlockPos ? (BlockPos) w : biome instanceof BlockPos ? (BlockPos) biome : BlockPos.ORIGIN;
        cir.setReturnValue(b == null ? 0.8F : b.getTemperature(p));        // vanilla temperature, no season shift
    }

    private static void pride$worldBiome(Object a, Object b, CallbackInfoReturnable<Boolean> cir, String m) {
        if (a instanceof World && b instanceof Biome && ((Biome) b).delegate != null) return;
        com.dogpound.patches.SereneSeasonsLog.once(m, a, b);
        // swapped: answer like vanilla does with the right objects (Biome.canRain)
        if (a instanceof Biome) cir.setReturnValue(((Biome) a).canRain());
        else if (b instanceof Biome) cir.setReturnValue(((Biome) b).canRain());
        else cir.setReturnValue(false);
    }

    private static void pride$worldPos(Object w, Object pos, CallbackInfoReturnable<Boolean> cir, String m) {
        if (w instanceof World && pos instanceof BlockPos) return;
        com.dogpound.patches.SereneSeasonsLog.once(m, w, pos);
        cir.setReturnValue(false);
    }
}
