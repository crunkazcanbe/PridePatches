package com.dogpound.patches.mixin;

import com.dogpound.patches.CreativeGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Clicking the creative-inventory tab page arrows advanced the page AND slammed the whole screen
 * shut, so every page turn meant reopening the inventory. Every close path -- the inventory key,
 * EntityPlayerSP.closeScreen(), a mod's own handler -- ends up at Minecraft.displayGuiScreen(null).
 * Refuse that one call while a click is in flight inside the creative screen.
 *
 * Deliberately narrow: only null (close), only during a creative click. Pressing E still closes the
 * inventory, and every other screen behaves exactly as before. The offender is logged once with a
 * stack trace so the real culprit is still identifiable.
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraftCreativeGuard {

    // Minecraft.displayGuiScreen(GuiScreen)
    @Inject(method = "func_147108_a(Lnet/minecraft/client/gui/GuiScreen;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$keepCreativeOpen(GuiScreen screen, CallbackInfo ci) {
        GuiScreen cur = Minecraft.getMinecraft().currentScreen;
        if (screen != null) {                                   // a screen opened from the inventory (a side-bar button)
            if (com.dogpound.patches.client.SideDock.applies(cur)) { CreativeGuard.openedFromInventory = screen; CreativeGuard.openedAt = System.currentTimeMillis(); }
            return;
        }
        if (cur != null && cur == CreativeGuard.openedFromInventory && System.currentTimeMillis() - CreativeGuard.openedAt < 250) {
            if (!CreativeGuard.reportedInv) {
                CreativeGuard.reportedInv = true;
                System.out.println("[DogPoundPatches] Blocked an instant close of " + cur.getClass().getName()
                        + " opened from the inventory. Culprit stack trace follows:");
                new Throwable("instant close blocked").printStackTrace(System.out);
            }
            ci.cancel();
            return;
        }
        if (!CreativeGuard.inClick) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative)) return;

        if (!CreativeGuard.reported) {
            CreativeGuard.reported = true;
            System.out.println("[DogPoundPatches] Blocked a close of the creative inventory during a "
                    + "click. Culprit stack trace follows:");
            new Throwable("creative inventory close blocked").printStackTrace(System.out);
        }
        ci.cancel();
    }

    /**
     * PolyPatcher's Inventory Scale draws containers at their own GUI scale. A screen opened FROM the inventory
     * (Nutrition, RealmCoin's hub...) was sized while the inventory was still current, so it got the inventory's
     * scale but drew at the normal one: squashed into a corner, buttons not under their pictures (her report
     * 2026-09-28). Size it again now that it is the current screen.
     */
    // Not right away: a REAL click runs inside PolyPatcher's container mouse handling, which swaps the inventory scale
    // in while it runs, so measuring now gets the wrong size again (her report: my test clicks looked right, hers
    // didn't). Next tick, before input, nothing is swapped.
    @Inject(method = "func_71407_l()V", at = @At("HEAD"), remap = false)   // runTick
    private void dpp$resizeFromInventory(CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiScreen screen = mc.currentScreen;
        if (screen == null || screen != CreativeGuard.openedFromInventory || CreativeGuard.resized == screen
                || screen instanceof net.minecraft.client.gui.inventory.GuiContainer) return;
        CreativeGuard.resized = screen;
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(mc);
        if (screen.width != sr.getScaledWidth() || screen.height != sr.getScaledHeight())
            screen.setWorldAndResolution(mc, sr.getScaledWidth(), sr.getScaledHeight());
    }
}
