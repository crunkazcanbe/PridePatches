package com.dogpound.patches.mixin;

import com.dogpound.patches.JsgShieldMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Just Stargate makes its "armor_shield" material with EnumHelper (and drops the result), then looks it up by name with
 * ArmorMaterial.valueOf(). On this pack's Java that lookup sometimes can't see the new constant ("No enum constant
 * ArmorMaterial.armor_shield": crash-2026-10-03_13.56.16, and at boot 2026-10-04 04:14). Keep the material the moment
 * it is made and hand exactly that one back, so no lookup is needed.
 */
@Mixin(targets = "tauri.dev.jsg.item.armor.AncientShield", remap = false)
public abstract class MixinJsgAncientShield {
    @Redirect(method = "<clinit>", require = 0, at = @At(value = "INVOKE",
              target = "Lnet/minecraftforge/common/util/EnumHelper;addArmorMaterial(Ljava/lang/String;Ljava/lang/String;I[IILnet/minecraft/util/SoundEvent;F)Lnet/minecraft/item/ItemArmor$ArmorMaterial;"))
    private static ItemArmor.ArmorMaterial pride$keep(String name, String texture, int durability, int[] reduction, int ench, SoundEvent sound, float tough) {
        ItemArmor.ArmorMaterial m = EnumHelper.addArmorMaterial(name, texture, durability, reduction, ench, sound, tough);
        JsgShieldMaterial.made = m;
        return m;
    }

    @Redirect(method = "<init>", require = 0,
              at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemArmor$ArmorMaterial;valueOf(Ljava/lang/String;)Lnet/minecraft/item/ItemArmor$ArmorMaterial;"))
    private static ItemArmor.ArmorMaterial pride$findMaterial(String name) {
        ItemArmor.ArmorMaterial kept = JsgShieldMaterial.made;
        if (kept != null && kept.name().equalsIgnoreCase(name)) return kept;
        for (ItemArmor.ArmorMaterial m : ItemArmor.ArmorMaterial.values())
            if (m.name().equalsIgnoreCase(name)) return m;
        return ItemArmor.ArmorMaterial.valueOf(name);
    }
}
