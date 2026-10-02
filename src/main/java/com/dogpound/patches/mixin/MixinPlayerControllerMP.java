package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Broad load-frame guard: PlayerControllerMP.getBlockReachDistance (func_78757_d)
 * reads mc.player.getEntityAttribute(...). Mods that tick during world load (Custom
 * NPCs, etc.) call it before the player exists -> NPE -> client dies, world freezes.
 * Return a sane default reach while there's no player; fixes every caller at once.
 */
@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {
    @Inject(method = "func_78757_d()F", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$reachWhenNoPlayer(CallbackInfoReturnable<Float> cir) {
        if (Minecraft.getMinecraft().player == null) {
            cir.setReturnValue(4.5F);
        }
    }
}
