package com.dogpound.patches.mixin;

import org.cyclops.cyclopscore.config.ConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * CyclopsCore flips "registryEventPassed" on ANY RegistryEvent.Register — including a custom registry some other
 * mod fires early — then throws "Tried registering X after its registration event" for every later Cyclops mod
 * (CapabilityProxy, Integrated*, EvilCraft, ColossalChests…). Always queue instead: queued entries are registered
 * when their own registry's event fires, which for Block/Item is still ahead of us in preInit.
 */
@Mixin(value = ConfigHandler.class, remap = false)
public abstract class MixinCyclopsEarlyRegistryEvent {
    @Redirect(method = "registerToRegistry(Lnet/minecraftforge/registries/IForgeRegistry;Lnet/minecraftforge/registries/IForgeRegistryEntry;Ljava/util/concurrent/Callable;)V",
            at = @At(value = "FIELD", target = "Lorg/cyclops/cyclopscore/config/ConfigHandler;registryEventPassed:Z"), remap = false)
    private boolean dpp$neverTooLate(ConfigHandler self) { return false; }
}
