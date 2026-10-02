package com.dogpound.patches.mixin;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * End Expansion's world start: every saved structure piece reloads its .nbt template, and each load goes through
 * Forge's CompoundDataFixer — template files never carry ForgeDataVersion, so every mod's walkers (OpenBlocks'
 * ResourceDataWalker over every spawner/chest item) run over megabytes of NBT (2026-09-30/10-01 dumps: 15+ min).
 * All 257 End Expansion templates are DataVersion 1343 = current 1.12.2, so they need no fixing at all:
 * read them straight from the jar, once, no fixer. Anything older or missing falls back to the vanilla path.
 */
@Mixin(targets = "com.example.structure.world.misc.ModStructureTemplate", remap = false)
public abstract class MixinEndExpansionTemplateCache {
    private static final ConcurrentHashMap<ResourceLocation, Template> dpp$templates = new ConcurrentHashMap<>();

    @Redirect(method = "loadTemplate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/gen/structure/template/TemplateManager;func_186237_a(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/world/gen/structure/template/Template;"),
            remap = false)
    private Template dpp$cached(TemplateManager tm, MinecraftServer server, ResourceLocation id) {
        Template t = dpp$templates.get(id);
        if (t != null) return t;
        t = dpp$readUnfixed(id);
        if (t == null) t = tm.getTemplate(server, id);
        Template prev = dpp$templates.putIfAbsent(id, t);
        return prev != null ? prev : t;
    }

    private static Template dpp$readUnfixed(ResourceLocation id) {
        try (InputStream in = MinecraftServer.class.getResourceAsStream("/assets/" + id.getResourceDomain() + "/structures/" + id.getResourcePath() + ".nbt")) {
            if (in == null) return null;
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(in);
            if (!nbt.hasKey("DataVersion", 99) || nbt.getInteger("DataVersion") < 1343) return null;   // old data: let the fixer run
            Template t = new Template();
            t.read(nbt);
            return t;
        } catch (Throwable e) {
            return null;
        }
    }
}
