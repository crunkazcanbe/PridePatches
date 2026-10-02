package com.dogpound.patches.mixin;

import com.dogpound.patches.GenGuard;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Flags the modded-worldgen pass so MixinWorld can refuse cross-chunk loads during it. */
@Mixin(value = GameRegistry.class, remap = false)
public abstract class MixinGameRegistry {

    private static boolean dpp$announced = false;

    @Inject(method = "generateWorld", at = @At("HEAD"), remap = false)
    private static void dpp$wgHead(int chunkX, int chunkZ, World world,
                                   IChunkGenerator g, IChunkProvider p, CallbackInfo ci) {
        if (!dpp$announced) {
            dpp$announced = true;
            System.out.println("[DogPoundPatches] worldgen guard ACTIVE (anti-cascade engaged)");
        }
        GenGuard.ACTIVE.set(Boolean.TRUE);
    }

    @Inject(method = "generateWorld", at = @At("RETURN"), remap = false)
    private static void dpp$wgRet(int chunkX, int chunkZ, World world,
                                  IChunkGenerator g, IChunkProvider p, CallbackInfo ci) {
        GenGuard.ACTIVE.set(Boolean.FALSE);
    }
}
