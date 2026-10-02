package com.dogpound.patches.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityScarecrow;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Touhou Little Maid makes creepers flee scarecrows by casting EntityAITasks.taskEntries to LinkedHashSet — a
 * performance mod in the pack swaps that set for a ConcurrentHashMap key set, so the cast crashed the server tick
 * (2026-10-01 test #17). Same behaviour (avoid scarecrow 10 blocks, speed 1.0/1.2, priority 1, put first) on any Set.
 */
@Mixin(targets = "com.github.tartaricacid.touhoulittlemaid.event.CreeperJoinWorldEvent", remap = false)
public abstract class MixinTouhouCreeperScarecrow {
    @Inject(method = "onCreeperJoinWorld", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dpp$anySet(EntityJoinWorldEvent event, CallbackInfo ci) {
        ci.cancel();
        if (!(event.getEntity() instanceof EntityCreeper)) return;
        EntityCreeper creeper = (EntityCreeper) event.getEntity();
        EntityAITasks tasks = creeper.tasks;
        Set<EntityAITasks.EntityAITaskEntry> set = tasks.taskEntries;
        List<EntityAITasks.EntityAITaskEntry> old = new ArrayList<>(set);
        set.clear();
        set.add(tasks.new EntityAITaskEntry(1, new EntityAIAvoidEntity<>(creeper, EntityScarecrow.class, 10.0F, 1.0D, 1.2D)));
        set.addAll(old);
    }
}
