package com.dogpound.patches.mixin;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.PlayerCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * RealWorld's hanging plant asks the CLIENT player "are you in creative?" when its support goes away. During world
 * generation there is no player yet: NullPointerException, crash (2026-10-03, a Lucraft meteorite next to a hanging
 * plant inside a Deeper Depths trial chamber while a new world was being created). No player = not creative.
 */
@Mixin(targets = "realworld.block.BlockRWHangingPlant", remap = false)
public abstract class MixinRealWorldHangingPlantNoPlayer {
    private static final PlayerCapabilities PRIDE$NOT_CREATIVE = new PlayerCapabilities();

    @Redirect(method = "func_189540_a", require = 0,
            at = @At(value = "FIELD", opcode = org.objectweb.asm.Opcodes.GETFIELD,
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;field_71075_bZ:Lnet/minecraft/entity/player/PlayerCapabilities;"))
    private PlayerCapabilities pride$caps(EntityPlayerSP player) {
        return player == null ? PRIDE$NOT_CREATIVE : player.capabilities;
    }
}
