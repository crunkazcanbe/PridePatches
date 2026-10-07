package com.dogpound.patches.mixin;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.IDataFixer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * OpenMods' data walker has the same per-block-entity {@code new ResourceLocation(id)} as vanilla's Filtered (see
 * {@link MixinFilteredFastId}); after that fix it was the top frame while MCreator Dungeons loaded its structures in a
 * fresh world (2026-10-04, TPS 1.4). Compare the strings instead.
 */
@Pseudo
@Mixin(targets = "openmods.fixers.ResourceDataWalker", remap = false)
public abstract class MixinOpenModsWalkerFastId {
    @Shadow @Final private net.minecraftforge.registries.IForgeRegistryEntry<?> entry;
    @Shadow @Final private String idTag;

    @Shadow protected abstract NBTTagCompound processImpl(IDataFixer fixer, NBTTagCompound compound, int versionIn);

    @Inject(method = "func_188266_a", at = @At("HEAD"), cancellable = true, require = 0)
    private void pride$fastId(IDataFixer fixer, NBTTagCompound compound, int versionIn, CallbackInfoReturnable<NBTTagCompound> cir) {
        ResourceLocation name = entry.getRegistryName();
        if (name == null) return;
        String id = compound.getString(idTag);
        int colon = id.indexOf(':');
        if (colon == 0 || colon == 1) return; // vanilla's one-letter-namespace rule: let it decide
        String ns = name.getResourceDomain(), path = name.getResourcePath(); // both already lower-case
        boolean match = colon > 1
                ? colon == ns.length() && id.length() == colon + 1 + path.length()
                    && id.regionMatches(true, 0, ns, 0, colon) && id.regionMatches(true, colon + 1, path, 0, path.length())
                : "minecraft".equals(ns) && path.equalsIgnoreCase(id);
        cir.setReturnValue(match ? processImpl(fixer, compound, versionIn) : compound);
    }
}
