package com.dogpound.patches.mixin;

import com.dogpound.patches.AnomalyWells;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Registers each anomaly well tile so MixinAnomalyWellCollision knows whether any exist. */
@Mixin(targets = "therealpant.thaumicattempts.world.tile.TileAnomalyWell", remap = false)
public abstract class MixinAnomalyWellTile {
    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void dpp$track(CallbackInfo ci) {
        AnomalyWells.add((TileEntity) (Object) this);
    }
}
