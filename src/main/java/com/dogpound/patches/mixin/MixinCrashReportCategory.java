package com.dogpound.patches.mixin;

import net.minecraft.crash.CrashReportCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes crash reports survive being written, so a crash tells you what actually broke.
 *
 * <p>{@code CrashReportCategory} compares stack frames with
 * {@code element.getFileName().equals(...)}. On modern JVMs {@code getFileName()} returns
 * <b>null</b> for frames with no source file — lambdas, method handles, generated
 * accessors, and anything Foundation/Cleanroom spins up — so the comparison throws a
 * NullPointerException <i>while Minecraft is building the crash report</i>.
 *
 * <p>The result is the worst possible failure mode: the real exception is thrown away and
 * replaced by "Unable to launch: Cannot invoke String.equals(Object) because the return
 * value of StackTraceElement.getFileName() is null", with no crash-report file written at
 * all. Seen on 2026-09-12, where it hid the true cause of a startup crash completely.
 *
 * <p>Fix: hand those comparisons an empty string instead of null. A frame with no file
 * name simply fails to match, which is the correct answer anyway, and the report gets
 * written naming the actual culprit.
 *
 * <p>Sister fix to {@link MixinNormalAsmModIdentifier}, which repairs the other way this
 * pack loses crash reports.
 */
@Mixin(CrashReportCategory.class)
public abstract class MixinCrashReportCategory {

    /**
     * Targets are the SRG names: the deobfuscated aliases have no obfuscation mapping in
     * this toolchain and the annotation processor rejects them outright.
     * {@code require = 0}: whichever of the two actually carries the comparison gets
     * patched, and a target that isn't there is not worth failing the whole mixin over.
     */
    @Redirect(
            method = { "func_85069_a", "func_85070_a" },
            at = @At(value = "INVOKE", target = "Ljava/lang/StackTraceElement;getFileName()Ljava/lang/String;"),
            require = 0, expect = 0)
    private String dpp$fileNameNeverNull(StackTraceElement element) {
        String name = element.getFileName();
        return name == null ? "" : name;
    }
}
