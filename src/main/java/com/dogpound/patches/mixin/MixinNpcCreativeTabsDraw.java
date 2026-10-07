package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Requested ("just remove them"): Custom NPCs' two creative-inventory tabs (ids 150/151) kept drawing on
 * top of the side dock whatever the dock did — their button has its own draw code. Stop it drawing there.
 * (Factions + Quests stay reachable from the survival inventory's top tabs.)
 */
@Mixin(targets = "noppes.npcs.client.gui.util.GuiNpcButton", remap = false)
public abstract class MixinNpcCreativeTabsDraw {
    @Inject(method = "func_191745_a(Lnet/minecraft/client/Minecraft;IIF)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$noCreativeTab(Minecraft mc, int mx, int my, float pt, CallbackInfo ci) {
        if (com.dogpound.patches.client.SideDock.suppressed((net.minecraft.client.gui.GuiButton) (Object) this)) ci.cancel();
    }
}
