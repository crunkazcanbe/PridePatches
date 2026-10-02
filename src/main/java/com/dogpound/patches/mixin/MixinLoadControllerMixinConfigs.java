package com.dogpound.patches.mixin;

import net.minecraftforge.fml.common.LoadController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * At CONSTRUCTING, Cleanroom feeds every mod jar's manifest "MixinConfigs" to Mixins.addConfigurations in
 * one go. One bad entry (Embers 1.26.3 lists a config named just "embers") throws and aborts the rest of
 * that step, including every mod's late mixin loaders (EMI's HEI hooks, RandomComplement...). Add the
 * configs one at a time instead, logging and skipping the broken ones.
 */
@Mixin(value = LoadController.class, remap = false)
public abstract class MixinLoadControllerMixinConfigs {

    @Redirect(method = "distributeStateMessage(Lnet/minecraftforge/fml/common/LoaderState;[Ljava/lang/Object;)V",
              at = @At(value = "INVOKE", target = "Lorg/spongepowered/asm/mixin/Mixins;addConfigurations([Ljava/lang/String;)V"),
              require = 0, remap = false)
    private void dpp$addConfigsOneByOne(String[] configs) {
        for (String config : configs) {
            String c = config.trim();
            if (c.isEmpty()) continue;
            try {
                Mixins.addConfiguration(c);
            } catch (Throwable t) {
                System.out.println("[DogPoundPatches] skipped broken mixin config '" + c + "': " + t);
            }
        }
    }
}
