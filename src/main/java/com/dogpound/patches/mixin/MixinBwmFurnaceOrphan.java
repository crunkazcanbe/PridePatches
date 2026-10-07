package com.dogpound.patches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockFurnace;
import net.minecraft.tileentity.TileEntity;

/**
 * Better With Mods' furnace reads its block's FACING every tick. World generation can leave an orphan furnace tile
 * entity behind after a structure replaces the block (seen with OTG/Dregora: block = air at spawn), which crashed the
 * server at "Preparing spawn area 99%". Drop the orphan instead.
 */
@Pseudo
@Mixin(targets = "betterwithmods.common.blocks.tile.TileFurnace", remap = false)
public abstract class MixinBwmFurnaceOrphan {
    @Inject(method = "func_73660_a()V", at = @At("HEAD"), cancellable = true)
    private void pride$orphan(CallbackInfo ci) {
        TileEntity te = (TileEntity) (Object) this;
        if (te.getWorld() == null || te.getWorld().getBlockState(te.getPos()).getBlock() instanceof BlockFurnace) return;
        ci.cancel();
        te.invalidate();
    }
}
