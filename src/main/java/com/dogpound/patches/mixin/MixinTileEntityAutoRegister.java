package com.dogpound.patches.mixin;

import java.util.Locale;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A mod block whose tile entity class was never registered ("... is missing a mapping! This is a bug!") crashed the
 * whole world the moment that chunk was saved or sent (10-07: Lucraft's TileEntityExtractor in fresh NightTest
 * chunks). Register such a class under a unique pride_autofix:* id instead. Its data won't survive a reload (nothing
 * registers that id early), but the world keeps running.
 */
@Mixin(TileEntity.class)
public abstract class MixinTileEntityAutoRegister {
    @Inject(method = "func_189516_d", at = @At("HEAD"), remap = false) // writeInternal
    private void dpp$autoRegister(NBTTagCompound tag, CallbackInfoReturnable<NBTTagCompound> cir) {
        Class<? extends TileEntity> c = ((TileEntity) (Object) this).getClass();
        if (TileEntity.getKey(c) != null) return;
        synchronized (TileEntity.class) {
            if (TileEntity.getKey(c) != null) return;
            String id = "pride_autofix:" + c.getName().toLowerCase(Locale.ROOT).replace('.', '_').replace('$', '_');
            TileEntity.register(id, c);
            System.out.println("[PridePatches] " + c.getName() + " had no tile-entity registration (mod bug) - registered as " + id + " instead of crashing the world");
        }
    }
}
