package com.dogpound.patches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Old-GPU crash guard for LambdaLib2 (bundled inside AcademyCraft).
 * <p>
 * {@code DebugDraw.postRenderEntity} lazily compiles a developer debug shader
 * ({@code /assets/lambdalib2/shader/DebugDraw.glsl}) the first time entities are rendered. On
 * older GPUs / GL drivers (e.g. her Mac's) that shader fails to compile and throws
 * {@code RuntimeException: Error loading shader script}, which crashes on world load — every time.
 * <p>
 * The whole feature is a dev-only debug-sphere overlay that never runs in normal play (there are
 * no debug spheres queued), so cancelling this render pass removes the crash with zero gameplay
 * impact. remap=false, targeted by name so we need no compile-time reference to LambdaLib2.
 */
@Mixin(targets = "cn.lambdalib2.util.DebugDraw", remap = false)
public abstract class MixinLambdaDebugDraw {

    @Inject(method = "postRenderEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$skipDebugShader(CallbackInfo ci) {
        ci.cancel();
    }
}
