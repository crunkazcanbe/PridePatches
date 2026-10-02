package com.dogpound.patches.mixin;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Companion to {@link MixinLeafiaAddonHazards}: AddonAssemblerRecipes.register references
 * the removed ModItems.coil_advanced_alloy -> NoSuchFieldError. Redirect to AIR so the
 * addon's recipes register and just skip that one stale recipe. remap=false.
 */
@Mixin(targets = "com.leafia.init.recipes.AddonAssemblerRecipes", remap = false)
public abstract class MixinLeafiaAddonAssemblerRecipes {
    @Redirect(method = "register()V",
              at = @At(value = "FIELD", target = "Lcom/hbm/items/ModItems;coil_advanced_alloy:Lnet/minecraft/item/Item;"),
              remap = false)
    private static Item dpp$noCoilAdvancedAlloy() {
        return Item.getByNameOrId("hbm:coil_tungsten"); // advanced-alloy coil -> closest existing coil
    }
}
