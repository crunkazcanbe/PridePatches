package com.dogpound.patches.mixin;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * NTM CE 2.5.0.4 removed the "advanced alloy" items ("port: bye advanced alloy"), but
 * Leafia's Cursed Addon (unmaintained) still references ModItems.blades_advanced_alloy in
 * AddonHazards.register -> NoSuchFieldError at startup. Redirect that one removed-item
 * read to AIR so the addon loads and just skips that stale hazard entry. remap=false.
 */
@Mixin(targets = "com.leafia.init.AddonHazards", remap = false)
public abstract class MixinLeafiaAddonHazards {
    @Redirect(method = "register()V",
              at = @At(value = "FIELD", target = "Lcom/hbm/items/ModItems;blades_advanced_alloy:Lnet/minecraft/item/Item;"),
              remap = false)
    private static Item dpp$noBladesAdvancedAlloy() {
        return Items.AIR; // AIR — harmless placeholder
    }
}
