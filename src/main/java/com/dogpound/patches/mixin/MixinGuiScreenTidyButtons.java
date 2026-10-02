package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Inventory buttons stacked on top of each other (her report 2026-09-28: Custom NPCs + Techguns + more). Runs after
 * setWorldAndResolution — i.e. after initGui AND every mod's InitGuiEvent.Post — so it sees the final button list.
 * The work is in ButtonTidy. remap=false (vanilla SRG), like the other patches here.
 */
@Mixin(GuiScreen.class)
public abstract class MixinGuiScreenTidyButtons {
    @Inject(method = "func_146280_a(Lnet/minecraft/client/Minecraft;II)V", at = @At("TAIL"), remap = false)
    private void dpp$tidyButtons(Minecraft mc, int w, int h, CallbackInfo ci) {
        com.dogpound.patches.client.SideDock.reset((GuiScreen) (Object) this);
        com.dogpound.patches.client.ButtonTidy.tidy((GuiScreen) (Object) this);
    }

    /** Again every frame, right before buttons draw: Quark / Recipe Handler re-place their buttons AFTER init
     *  (2026-09-28 dump: Recipe Handler's creative button sat on Quark's chest button and init-time tidy never saw it). */
    @Inject(method = "func_73863_a(IIF)V", at = @At("HEAD"), remap = false)
    private void dpp$tidyBeforeDraw(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        com.dogpound.patches.client.ButtonTidy.tidy((GuiScreen) (Object) this);
    }

    /** the side dock's ▲ ▼ arrows go on top of the buttons */
    @Inject(method = "func_73863_a(IIF)V", at = @At("TAIL"), remap = false)
    private void dpp$dockArrows(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        com.dogpound.patches.client.SideDock.drawArrows((GuiScreen) (Object) this, mouseX, mouseY);
    }

    /** arrow clicks and the mouse wheel over the dock column don't reach the screen underneath */
    @Inject(method = "func_146274_d()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$dockMouse(CallbackInfo ci) {
        if (com.dogpound.patches.client.SideDock.applies((GuiScreen) (Object) this) && com.dogpound.patches.client.SideDock.mouse((GuiScreen) (Object) this)) ci.cancel();
    }
}
