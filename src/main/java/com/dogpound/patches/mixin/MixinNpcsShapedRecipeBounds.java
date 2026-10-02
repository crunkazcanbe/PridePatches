package com.dogpound.patches.mixin;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Custom NPCs' shaped-recipe check (NpcShapedRecipes.matches) loops over the recipe's trimmed width but indexes
 * the ingredient list with the full width, so recipes like npc_wand / mob_cloner ask for slots past the end. It
 * caught that, logged an ERROR, and used an empty ingredient — 1,500+ error lines per startup (2026-09-28) and more
 * every time anything crafted. Same result (empty ingredient), no exception, no log line.
 */
@Mixin(targets = "noppes.npcs.items.crafting.NpcShapedRecipes", remap = false)
public abstract class MixinNpcsShapedRecipeBounds {
    @Redirect(method = "func_77569_a",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/util/NonNullList;get(I)Ljava/lang/Object;"))
    private Object dpp$safeIngredient(NonNullList<?> ingredients, int index) {
        return index >= 0 && index < ingredients.size() ? ingredients.get(index) : Ingredient.EMPTY;
    }
}
