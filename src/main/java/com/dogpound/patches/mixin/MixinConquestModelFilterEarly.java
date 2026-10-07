package com.dogpound.patches.mixin;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Startup crash (Pride pack, 2026-10-07): Fossils & Archeology's FAMachineRecipeRegistry.init asks every
 * item for its sub-items during item REGISTRATION; Conquest Reforged's ModelFilterList.add then looks the stack's item
 * model up through Minecraft.getRenderItem(), which doesn't exist yet -> NPE "Initializing game". Before the item
 * renderer exists there's nothing to filter on, so just add the stack.
 */
@Pseudo
@Mixin(targets = "com.conquestreforged.client.ModelFilterList", remap = false)
public abstract class MixinConquestModelFilterEarly {
    @Shadow(remap = false) @Final private List<ItemStack> delegate;

    @Inject(method = "add(ILnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$noRendererYet(int index, ItemStack stack, CallbackInfo ci) {
        if (Minecraft.getMinecraft().getRenderItem() != null) return;
        delegate.add(index, stack);
        ci.cancel();
    }
}
