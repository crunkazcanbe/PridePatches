package com.dogpound.patches.mixin;

import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Custom NPCs lists every built-in DamageSource's name; one mod leaves damageType null, so `name.equals(...)` threw a
 * NullPointerException that got logged as an ERROR (20x per startup). The original then skipped that entry; a null
 * name now reads as "generic", which the same loop already skips — same outcome, no exception.
 */
@Mixin(targets = "noppes.npcs.entity.data.Resistances", remap = false)
public abstract class MixinNpcsResistancesNullDamage {
    @Redirect(method = "loadAllDamages",
              at = @At(value = "FIELD", target = "Lnet/minecraft/util/DamageSource;field_76373_n:Ljava/lang/String;"))
    private static String dpp$nameOrSkip(DamageSource source) {
        return source == null || source.damageType == null ? "generic" : source.damageType;
    }
}
