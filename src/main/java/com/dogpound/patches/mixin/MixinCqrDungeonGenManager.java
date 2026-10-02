package com.dogpound.patches.mixin;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * CQR removes a world's DungeonGenerationManager on unload, but the world can still get one more tick —
 * INSTANCES.get(world) is null and the server tick loop dies (crashed on every quit, 2026-09-25).
 */
@Mixin(targets = "team.cqr.cqrepoured.world.structure.generation.generation.DungeonGenerationManager", remap = false)
public abstract class MixinCqrDungeonGenManager {
    @Shadow(remap = false) @Final private static Map<World, ?> INSTANCES;

    @Inject(method = "onWorldTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dpp$skipUnloadedWorld(World world, CallbackInfo ci) {
        if (INSTANCES.get(world) == null) ci.cancel();
    }
}
