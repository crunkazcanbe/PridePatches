package com.dogpound.patches.mixin;

import java.io.File;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.montoyo.mcef.utilities.IProgressListener;
import net.montoyo.mcef.utilities.Util;

/**
 * MCEF fetches mcef.json and the JCEF checksum from its mirror on EVERY launch, even with skipUpdates=true.
 * When the mirror (cdn.oxmc.me) hangs, startup waits on each request. If the browser is already installed,
 * use the local copies instead; a fresh install still downloads as normal.
 */
@Mixin(targets = "net.montoyo.mcef.remote.RemoteConfig", remap = false)
public abstract class MixinMcefOffline {

    @Redirect(method = "readConfig()Lcom/google/gson/JsonObject;", at = @At(value = "INVOKE",
            target = "Lnet/montoyo/mcef/utilities/Util;downloadFileSafe(Ljava/lang/String;Ljava/io/File;Lnet/montoyo/mcef/utilities/IProgressListener;)Z"))
    private boolean pride$localConfig(String url, File dest, IProgressListener l) {
        File local = new File(dest.getParentFile(), "mcef.json");
        if (local.isFile() && local.length() > 0) {
            System.out.println("[PridePatches] MCEF: using the local mcef.json (skipping " + url + ")");
            return false;                                  // MCEF: "Couldn't read remote config. Using local configuration file."
        }
        return Util.downloadFileSafe(url, dest, l);
    }

    @Redirect(method = "downloadMCEF", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/montoyo/mcef/utilities/Util;downloadFile(Ljava/lang/String;Ljava/io/File;Lnet/montoyo/mcef/utilities/IProgressListener;)V"))
    private void pride$localChecksum(String url, File dest, IProgressListener l) throws java.io.IOException {
        String p = dest.getPath();
        if (url.endsWith(".sha256") && p.endsWith(".temp")) {
            File local = new File(p.substring(0, p.length() - 5));
            File runtime = new File(dest.getParentFile(), "libcef.so");
            if (local.isFile() && (runtime.isFile() || new File(dest.getParentFile(), "libcef.dll").isFile())) {
                java.nio.file.Files.copy(local.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[PridePatches] MCEF: browser already installed, skipping the checksum download");
                return;                                    // contents match → "JCEF build already downloaded, Skipping download."
            }
        }
        Util.downloadFile(url, dest, l);
    }
}
