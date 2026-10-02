package com.dogpound.patches.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** a removed button (see SideDock.suppressed) can't be clicked either */
@Mixin(GuiButton.class)
public abstract class MixinSuppressedButtonClick {
    @Inject(method = "func_146116_c(Lnet/minecraft/client/Minecraft;II)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$noClick(Minecraft mc, int mx, int my, CallbackInfoReturnable<Boolean> cir) {
        if (com.dogpound.patches.client.SideDock.suppressed((GuiButton) (Object) this)) cir.setReturnValue(false);
    }
}
