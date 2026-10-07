package com.dogpound.patches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.World;

/**
 * OTG names an OTG world's dimension after its DimensionType, and only knows the overworld as "overworld" (or the
 * world's own name). Something in the Pride pack renames dimension 0's type, so OTG found no settings for it and
 * crashed creating every OTG world ("Cannot read field PresetName ... getDimensionConfig(String) is null").
 * Dimension 0 of an OTG world is always the overworld.
 */
@Pseudo
@Mixin(targets = "com.pg85.otg.forge.world.WorldHelper", remap = false)
public abstract class MixinOtgOverworldName {
    private static boolean pride$logged;

    @Inject(method = "getName(Lnet/minecraft/world/World;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private static void pride$overworld(World world, CallbackInfoReturnable<String> cir) {
        if (world == null || world.provider == null || world.provider.getDimension() != 0) return;
        if (!"OpenTerrainGenerator".equals(world.getWorldInfo().getGeneratorOptions())) return;
        if (!pride$logged) {
            pride$logged = true;
            String real = net.minecraftforge.common.DimensionManager.getProviderType(0).getName();
            if (!"overworld".equals(real))
                System.out.println("[PridePatches] OTG: dimension 0's type is named '" + real + "' (not 'overworld'); treating it as the overworld");
        }
        cir.setReturnValue("overworld");
    }
}
