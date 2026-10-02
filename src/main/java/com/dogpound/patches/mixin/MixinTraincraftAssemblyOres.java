package com.dogpound.patches.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Traincraft's Assembly Table recipes nest loops over whole ore lists (plankWood × logWood × ingotSteel × ingotIron
 * × coal × dyes…) and register one recipe per combination. At ~700 mods those lists are long enough that Init never
 * finished (2026-10-01: >20 min, 100% CPU). Inside that one method each ore list yields only its first two entries —
 * the vanilla ones (wildcard planks/logs, vanilla iron/coal/dyes) — so the Assembly Table keeps every recipe it had.
 */
@Mixin(targets = "train.common.recipes.AssemblyTableRecipes", remap = false)
public abstract class MixinTraincraftAssemblyOres {
    @Redirect(method = "recipes", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/oredict/OreDictionary;getOres(Ljava/lang/String;)Lnet/minecraft/util/NonNullList;"), remap = false)
    private static NonNullList<ItemStack> dpp$firstTwo(String name) {
        NonNullList<ItemStack> all = OreDictionary.getOres(name);
        if (all.size() <= 2) return all;
        NonNullList<ItemStack> few = NonNullList.create();
        few.add(all.get(0));
        few.add(all.get(1));
        return few;
    }
}
