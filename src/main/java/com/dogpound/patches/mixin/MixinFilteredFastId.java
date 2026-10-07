package com.dogpound.patches.mixin;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.walkers.Filtered;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Every mod's data walker checks each block entity with {@code new ResourceLocation(nbt "id").equals(key)}. With ~770
 * mods that is hundreds of ResourceLocations per block entity, and Hydrogen + StellarCore both hook that constructor:
 * one Capsule reward structure took over a minute to load on the server thread (2026-10-04, 60/60 samples, TPS 3,
 * Distant Horizons paused). Compare the id string against the key directly — same answer, no allocation.
 */
@Mixin(value = Filtered.class, remap = false)
public abstract class MixinFilteredFastId {
    @Shadow @Final private ResourceLocation field_188272_a; // key
    @Unique private String pride$key;

    @Shadow public abstract NBTTagCompound func_188271_b(IDataFixer fixer, NBTTagCompound compound, int versionIn);

    @Inject(method = "func_188266_a", at = @At("HEAD"), cancellable = true)
    private void pride$fastId(IDataFixer fixer, NBTTagCompound compound, int versionIn, CallbackInfoReturnable<NBTTagCompound> cir) {
        if (field_188272_a == null) { cir.setReturnValue(compound); return; }
        String k = pride$key;
        if (k == null) pride$key = k = field_188272_a.toString();
        String id = compound.getString("id");
        int colon = id.indexOf(':');
        if (colon == 0 || colon == 1) return; // vanilla's odd one-letter-namespace rule: let it decide
        // ResourceLocation lower-cases both halves and defaults the namespace to "minecraft"
        boolean match = colon > 1
                ? id.length() == k.length() && k.equalsIgnoreCase(id)
                : id.length() + 10 == k.length() && k.startsWith("minecraft:") && k.regionMatches(true, 10, id, 0, id.length());
        cir.setReturnValue(match ? func_188271_b(fixer, compound, versionIn) : compound);
    }
}
