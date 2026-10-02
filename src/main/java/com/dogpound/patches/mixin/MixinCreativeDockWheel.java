package com.dogpound.patches.mixin;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** the creative screen scrolls its item grid with the wheel itself: the side dock gets first go */
@Mixin(GuiContainerCreative.class)
public abstract class MixinCreativeDockWheel {
    @Inject(method = "func_146274_d()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$dockFirst(CallbackInfo ci) {
        if (com.dogpound.patches.client.SideDock.mouse((GuiScreen) (Object) this)) ci.cancel();
    }
}
