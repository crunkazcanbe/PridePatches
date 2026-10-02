package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skip Custom NPCs' client tick (noppes.npcs.client.ClientTickHandler.npcClientTick)
 * during world load while mc.player is null — it derefs the player (block reach, etc.).
 * remap=false; target resolved via a compile-only stub (see src/.../noppes/, excluded
 * from the jar). The real Custom NPCs class is patched at runtime.
 */
@Mixin(targets = "noppes.npcs.client.ClientTickHandler", remap = false)
public abstract class MixinNpcsClientTickHandler {
    @Inject(method = "npcClientTick(Lnet/minecraftforge/fml/common/gameevent/TickEvent$ClientTickEvent;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$skipNpcTickWhenNoPlayer(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        if (Minecraft.getMinecraft().player == null) {
            ci.cancel();
        }
    }
}
