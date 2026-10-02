package com.dogpound.patches.mixin;

import com.dogpound.patches.CreativeGuard;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Raises CreativeGuard.inClick for the duration of a click inside the creative inventory, and drops
 * it again on the next frame. Targets GuiScreen rather than GuiContainerCreative because the creative
 * screen does not declare mouseClicked itself -- it inherits it -- so there is no method there to
 * inject into. remap=false: SRG names, matching the rest of this mod.
 *
 * Cleared in drawScreen (next frame) instead of at RETURN so a mod that throws out of the click
 * handler cannot strand the flag on forever.
 */
@Mixin(GuiScreen.class)
public abstract class MixinCreativeClickGuard {

    // GuiScreen.mouseClicked(int, int, int)
    @Inject(method = "func_73864_a(III)V", at = @At("HEAD"), remap = false)
    private void dpp$creativeClickStart(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if ((Object) this instanceof GuiContainerCreative) CreativeGuard.inClick = true;
    }

    // GuiScreen.drawScreen(int, int, float) -- next frame, click is over
    @Inject(method = "func_73863_a(IIF)V", at = @At("HEAD"), remap = false)
    private void dpp$creativeClickEnd(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        CreativeGuard.inClick = false;
    }
}
