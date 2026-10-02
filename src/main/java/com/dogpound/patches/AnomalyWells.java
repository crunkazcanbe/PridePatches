package com.dogpound.patches;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/** Every Thaumic Attempts anomaly well tile ever created (weak, so unloaded ones drop out on GC). */
public final class AnomalyWells {
    private static final Set<TileEntity> WELLS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public static void add(TileEntity te) { WELLS.add(te); }

    /** True if a live well sits in this world — only then is the collision-box scan worth doing. */
    public static boolean anyIn(World world) {
        if (WELLS.isEmpty()) return false;
        synchronized (WELLS) {
            for (TileEntity te : WELLS) if (!te.isInvalid() && te.getWorld() == world) return true;
        }
        return false;
    }
}
