package com.dogpound.patches.mixin;

import java.util.concurrent.Callable;

import com.google.common.cache.Cache;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * CTM wraps item models and builds the per-item variant through a Guava cache. When the wrapped model's own item
 * override throws (EvilCraft's blood-filled Broom: UnsupportedOperationException), CTM lets it escape and the game
 * closes while just DRAWING the item — the 2026-10-02 creative sweep crash (crash-2026-10-02_21.33.28). If building the
 * CTM variant fails, the item is drawn with its normal model instead.
 */
@Mixin(targets = "team.chisel.ctm.client.model.AbstractCTMBakedModel$Overrides", remap = false)
public abstract class MixinCtmItemOverrideSafe {
    @Redirect(method = "handleItemState", require = 0,
              at = @At(value = "INVOKE", target = "Lcom/google/common/cache/Cache;get(Ljava/lang/Object;Ljava/util/concurrent/Callable;)Ljava/lang/Object;"))
    private Object pride$safeGet(Cache<Object, Object> cache, Object key, Callable<Object> loader,
                                 IBakedModel original, ItemStack stack, World world, EntityLivingBase entity) {
        try { return cache.get(key, loader); }
        catch (Throwable t) { return original; }
    }
}
