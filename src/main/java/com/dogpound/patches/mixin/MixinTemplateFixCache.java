package com.dogpound.patches.mixin;

import java.io.File;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.IFixType;
import net.minecraft.world.gen.structure.template.TemplateManager;

/**
 * Structure templates (More Creeps' castle, MCreator Dungeons, ...) get every mod's data fixes run over every
 * block and mob spawner on each launch — the castle alone froze world creation for ~4 minutes. Run the fixes
 * once, save the upgraded template to config/pridepatches/template-cache/, and load that on later launches.
 */
@Mixin(value = TemplateManager.class, remap = false)
public abstract class MixinTemplateFixCache {
    private static final ThreadLocal<String> PRIDE$ID = new ThreadLocal<>();

    @Inject(method = "func_186239_a(Ljava/lang/String;Ljava/io/InputStream;)V", at = @At("HEAD"))
    private void pride$rememberId(String id, java.io.InputStream in, CallbackInfo ci) { PRIDE$ID.set(id); }

    @Inject(method = "func_186239_a(Ljava/lang/String;Ljava/io/InputStream;)V", at = @At("RETURN"))
    private void pride$forgetId(String id, java.io.InputStream in, CallbackInfo ci) { PRIDE$ID.remove(); }

    @Redirect(method = "func_186239_a(Ljava/lang/String;Ljava/io/InputStream;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/datafix/DataFixer;func_188257_a(Lnet/minecraft/util/datafix/IFixType;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;"))
    private NBTTagCompound pride$cachedFix(DataFixer fixer, IFixType type, NBTTagCompound nbt) {
        String id = PRIDE$ID.get();
        // every template: even current ones go through every mod's walkers (~770 mods), MCreator Dungeons' first reads
        // alone held a fresh world at TPS 1.4 (2026-10-04)
        if (id == null || !nbt.hasKey("blocks")) return fixer.process(type, nbt);
        File f = new File("config/pridepatches/template-cache/" + id.replaceAll("[^a-zA-Z0-9._-]", "_") + "-" + Integer.toHexString(nbt.hashCode()) + ".nbt");
        try {
            if (f.isFile()) return CompressedStreamTools.read(f);
        } catch (Exception e) { System.out.println("[PridePatches] template cache read failed for " + id + ": " + e); }
        long t = System.currentTimeMillis();
        NBTTagCompound fixed = fixer.process(type, nbt);
        try {
            f.getParentFile().mkdirs();
            CompressedStreamTools.write(fixed, f);
            System.out.println("[PridePatches] upgraded template " + id + " once in " + (System.currentTimeMillis() - t) + " ms; cached for next launch");
        } catch (Exception e) { System.out.println("[PridePatches] template cache write failed for " + id + ": " + e); }
        return fixed;
    }
}
