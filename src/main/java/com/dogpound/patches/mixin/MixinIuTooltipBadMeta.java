package com.dogpound.patches.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Industrial Upgrade's tooltip handler turns EVERY block item's damage value into a block state with getStateFromMeta.
 * Blocks whose item meta isn't a valid state (Industrial Decor fences: facing=down; Futurepack thrusters: metadata=12)
 * throw IllegalArgumentException — and Item Borders asks for the tooltip of every slot it draws, so just scrolling
 * the creative inventory past one of them crashed the game ("Rendering screen", 2026-10-02 18:55 and 19:24).
 * A meta that isn't a valid state now falls back to the block's default state; everything else is unchanged.
 */
@Mixin(targets = "com.denfop.events.IUEventHandler", remap = false)
public abstract class MixinIuTooltipBadMeta {
    @Redirect(method = "addInformItem", require = 0,
              at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;func_176203_a(I)Lnet/minecraft/block/state/IBlockState;"))
    private IBlockState pride$safeState(Block block, int meta) {
        try { return block.getStateFromMeta(meta); }
        catch (RuntimeException e) { return block.getDefaultState(); }
    }
}
