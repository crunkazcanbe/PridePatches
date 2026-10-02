package com.dogpound.patches.mixin;

import java.io.File;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModContainerFactory;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.discovery.asm.ASMModParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Scala compiles an `object Foo` annotated @Mod into BOTH `Foo` and its companion `Foo$`, each carrying
 * @Mod. Classic Forge only built a container for `Foo`; Cleanroom builds one for each, so every Scala mod
 * (ProjectRed, OpenComputers, ForgeMultipart, MrTJPCore, bdlib, Gendustry...) died with
 * "Multiple entries with same key". Skipping the `$` companion restores the old behaviour for all of them.
 */
@Mixin(value = ModContainerFactory.class, remap = false)
public abstract class MixinModContainerFactory {

    @Inject(method = "build", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$skipScalaCompanion(ASMModParser parser, File source, ModCandidate candidate,
                                        CallbackInfoReturnable<ModContainer> cir) {
        String name = parser.getASMType().getClassName();
        if (name.endsWith("$")) {
            System.out.println("[DogPoundPatches] skipped Scala companion @Mod " + name + " (" + source.getName() + ")");
            cir.setReturnValue(null);   // both discoverers treat null as "not a mod"
        }
    }
}
