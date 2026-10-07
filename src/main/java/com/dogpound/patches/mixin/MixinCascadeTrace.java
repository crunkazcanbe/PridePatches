package com.dogpound.patches.mixin;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge only names the mod behind cascading worldgen, not the generator. Log one stack trace per mod (to the log files,
 * the console hides traces) so each one can get a +8 offset patch. Flying around 2026-10-03: IU 27, RecComplex 26,
 * DollsFrontLine 21, Depths Update 8 cascades in ~15 min.
 */
@Mixin(value = Chunk.class, remap = false)
public abstract class MixinCascadeTrace {
    private static final Set<String> PRIDE$SEEN = new HashSet<>();

    @Inject(method = "logCascadingWorldGeneration", at = @At("HEAD"), require = 0)
    private void pride$trace(CallbackInfo ci) {
        ModContainer mc = Loader.instance().activeModContainer();
        String id = mc == null ? "minecraft" : mc.getModId();
        if (PRIDE$SEEN.add(id)) LogManager.getLogger("PridePatches").warn("Cascading worldgen trace for " + id, new Throwable("cascade"));
    }
}
