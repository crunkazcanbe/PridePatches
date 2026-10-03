package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Industrial Upgrade builds its replicator tooltip lines in a SHARED field (tupleReplicatorRecipe) — checked, then
 * cleared/reset on the next tooltip. EMI bakes its search index on a background thread and asks for tooltips at the
 * same time the game draws them, so one thread nulled the field between the other's check and use → NPE crash
 * (crash-2026-10-02_21.53.08, IUEventHandler.addInfo:919). Industrial Upgrade's extra lines are now only built on
 * the game thread; background tooltip requests (EMI search) skip them. In-game tooltips are unchanged.
 */
@Mixin(targets = "com.denfop.events.IUEventHandler", remap = false)
public abstract class MixinIuTooltipMainThread {
    @Inject(method = "addInfo", at = @At("HEAD"), cancellable = true, require = 0)
    private void pride$onlyGameThread(ItemTooltipEvent e, CallbackInfo ci) {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) ci.cancel();
    }
}
