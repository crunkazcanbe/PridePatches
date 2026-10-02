package com.dogpound.patches.mixin;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Celeritas mipmap NPE guard (SURGICAL).
 *
 * Celeritas reimplements TextureAtlasSprite mipmap generation and calls
 * framesTextureData.getFirst() on it. For some animated sprites the first frame entry is
 * null, so getFirst() returns null and the following array load NPEs -> the ENTIRE texture
 * stitch crashes with "Applying mipmap" (seen on
 * industrialupgrade:blocks/alkalineearthquarry_right_active, 9 frames). Vanilla tolerates
 * this; Celeritas does not.
 *
 * Guard: before mipmaps are generated, if the sprite has no usable base frame, skip mipmap
 * generation for THAT one sprite (it just renders without mipmaps -> harmless; mipmap level
 * is 0 here anyway). Everything else stitches normally.
 *
 * SRG method name + remap=false for the Cleanroom runtime. The frame-data field is read
 * reflectively (trying the SRG name then the MCP name) so we don't depend on the dev(MCP)
 * vs runtime(SRG) field naming, and the whole guard is wrapped so it can never itself
 * crash the stitch.
 *
 * Lower priority value -> this HEAD callback runs before Celeritas' own weaving, so the
 * cancel happens before anything can dereference the null frame.
 */
@Mixin(value = TextureAtlasSprite.class, priority = 500)
public abstract class MixinTextureAtlasSpriteMipmap {

    private static Field dpp$framesField;
    private static boolean dpp$framesFieldResolved;

    @Inject(method = "func_147963_d(I)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$guardMipmap(int level, CallbackInfo ci) {
        try {
            if (!dpp$framesFieldResolved) {
                dpp$framesFieldResolved = true;
                for (String name : new String[]{"field_110976_a", "framesTextureData"}) {
                    try {
                        Field f = TextureAtlasSprite.class.getDeclaredField(name);
                        f.setAccessible(true);
                        dpp$framesField = f;
                        break;
                    } catch (NoSuchFieldException ignored) {
                        // try the next candidate name
                    }
                }
            }
            if (dpp$framesField == null) {
                return; // couldn't resolve the field; leave the original behaviour alone
            }
            Object data = dpp$framesField.get(this);
            if (!(data instanceof List)) {
                return;
            }
            List<?> frames = (List<?>) data;
            if (frames.isEmpty() || frames.get(0) == null) {
                ci.cancel(); // skip mipmap gen for this sprite -> no NPE, stitch continues
            }
        } catch (Throwable t) {
            // The guard must never crash the texture stitch itself.
        }
    }
}
