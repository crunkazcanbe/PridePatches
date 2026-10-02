package com.dogpound.patches.mixin;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MineColonies' ItemStorage.equals compares item NBT as `a.getTagCompound().equals(b.getTagCompound())` after only
 * checking that not BOTH are null — so an item without NBT compared with one that has NBT crashes the server tick
 * (NullPointerException in CompatibilityManager.discoverFuel, 2026-10-02, big pack). Null-safe compare, same answers.
 */
@Mixin(value = com.minecolonies.api.crafting.ItemStorage.class, remap = false)
public abstract class MixinColoniesItemStorageNbt {
    @Redirect(method = "equals", require = 0,
              at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;equals(Ljava/lang/Object;)Z"))
    private boolean pride$nullSafe(NBTTagCompound a, Object b) {
        return Objects.equals(a, b);
    }
}
