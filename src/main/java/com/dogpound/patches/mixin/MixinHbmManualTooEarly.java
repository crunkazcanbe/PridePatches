package com.dogpound.patches.mixin;

import java.util.function.Predicate;

import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * HBM's in-game manual (QMAW) rebuilds on every resource reload. Other mods force several reloads during pre-init, before
 * any item exists, so each early rebuild fails on every item icon: 2,754 "Error reading stack array … defaulting to
 * NOTHING" errors, each followed by a full stack dump (measured 2026-10-02, big pack). Skip the rebuilds until items are
 * registered; the game's final reload after loading builds the manual with real items.
 */
@Mixin(value = com.hbm.qmaw.QMAWLoader.class, remap = false)
public abstract class MixinHbmManualTooEarly {
    @Inject(method = "onResourceManagerReload", at = @At("HEAD"), cancellable = true)
    private void pride$notBeforeItems(IResourceManager manager, Predicate<IResourceType> types, CallbackInfo ci) {
        if (!Loader.instance().hasReachedState(LoaderState.INITIALIZATION)) ci.cancel();
    }
}
