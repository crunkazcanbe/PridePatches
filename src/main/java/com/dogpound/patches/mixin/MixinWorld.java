package com.dogpound.patches.mixin;

import com.dogpound.patches.GenGuard;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * While the modded-worldgen pass runs (GenGuard.ACTIVE), refuse any block write OR read OR
 * top-block lookup that targets a NOT-yet-loaded chunk — that's the access that force-loads a
 * neighbour and cascades. The loaded 2x2 populate area passes isBlockLoaded, so legit features
 * still generate. SRG method names + remap=false so it matches the Cleanroom runtime with no
 * refmap; bodies use MCP names that reobfJar converts.
 */
@Mixin(World.class)
public abstract class MixinWorld {

    // setBlockState(BlockPos, IBlockState, int) -> func_180501_a  (cross-chunk WRITE)
    @Inject(method = "func_180501_a(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$setBlock(BlockPos pos, IBlockState state, int flags, CallbackInfoReturnable<Boolean> cir) {
        if (GenGuard.ACTIVE.get() && !((World) (Object) this).isBlockLoaded(pos)) {
            dpp$count();
            cir.setReturnValue(Boolean.FALSE);
        }
    }

    // getBlockState(BlockPos) -> func_180495_p  (cross-chunk READ; return air instead of loading)
    @Inject(method = "func_180495_p(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$getBlock(BlockPos pos, CallbackInfoReturnable<IBlockState> cir) {
        if (GenGuard.ACTIVE.get() && !((World) (Object) this).isBlockLoaded(pos)) {
            dpp$count();
            cir.setReturnValue(Blocks.AIR.getDefaultState());
        }
    }

    // getTopSolidOrLiquidBlock(BlockPos) -> func_175672_r
    @Inject(method = "func_175672_r(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$top(BlockPos pos, CallbackInfoReturnable<BlockPos> cir) {
        if (GenGuard.ACTIVE.get() && !((World) (Object) this).isBlockLoaded(pos)) {
            dpp$count();
            cir.setReturnValue(pos);
        }
    }

    // getSkyColor(Entity, float) -> func_72833_a  (CLIENT render: guard the load-frame race)
    // The acid-rain fog renderer (leafia MixinEntityRenderer_AcidRain) can call this with a null
    // view entity for one frame while a world is still loading — especially when the saved player
    // is in an HBM space/celestial dimension (WorldProviderCelestial.getSkyColor -> getSkyColorBody
    // dereferences entityIn.posX). Return black (correct for space, invisible for one load frame)
    // instead of NPE-crashing the whole client.
    @Inject(method = "func_72833_a(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/util/math/Vec3d;",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void dpp$skyColorNullGuard(Entity entityIn, float partialTicks, CallbackInfoReturnable<Vec3d> cir) {
        if (entityIn == null) {
            cir.setReturnValue(new Vec3d(0.0D, 0.0D, 0.0D));
        }
    }

    private static void dpp$count() {
        GenGuard.blocked++;
        if (GenGuard.blocked == 1 || GenGuard.blocked % 200 == 0) {
            System.out.println("[DogPoundPatches] blocked " + GenGuard.blocked + " cross-chunk worldgen accesses (cascade prevented)");
        }
    }
}
