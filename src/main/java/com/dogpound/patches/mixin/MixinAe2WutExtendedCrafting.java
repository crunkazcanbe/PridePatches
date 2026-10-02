package com.dogpound.patches.mixin;

import com.blakebr0.extendedcrafting.crafting.table.TableRecipeManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * AE2WUT's all-in-one terminal moves to an Extended Crafting table recipe once it combines more than 9 terminals
 * (more AE2 add-ons at 700 mods), calling addShapeless(ItemStack, NonNullList) — gone in Extended Crafting 1.5.6,
 * which has addShapeless(ItemStack, Object...). Same recipe through the method that exists (2026-10-01 crash).
 */
@Mixin(targets = "com.circulation.ae2wut.recipes.AllWUTRecipe", remap = false)
public abstract class MixinAe2WutExtendedCrafting {
    @Redirect(method = "extendedcraftingRecipe", remap = false, at = @At(value = "INVOKE",
            target = "Lcom/blakebr0/extendedcrafting/crafting/table/TableRecipeManager;addShapeless(Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/NonNullList;)V"))
    private static void dpp$addShapeless(TableRecipeManager mgr, ItemStack out, NonNullList<?> ingredients) {
        mgr.addShapeless(out, ingredients.toArray());
    }
}
