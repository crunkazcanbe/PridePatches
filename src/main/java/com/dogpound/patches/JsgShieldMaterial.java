package com.dogpound.patches;

import net.minecraft.item.ItemArmor;

/** The armor material Just Stargate creates for its Ancient Shield, kept the moment it is made (see MixinJsgAncientShield). */
public final class JsgShieldMaterial {
    private JsgShieldMaterial() {}
    public static volatile ItemArmor.ArmorMaterial made;
}
