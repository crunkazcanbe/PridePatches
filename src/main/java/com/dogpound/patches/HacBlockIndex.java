package com.dogpound.patches;

import defeatedcrow.hac.api.climate.BlockSet;
import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Same answer as HeatBlockRegister.include (last entry whose block matches and whose meta matches or is the
 * 32767 wildcard), but via a per-set Block index instead of a full scan with equals().
 * ponytail: index rebuilds on size change only; an add+remove pair between lookups would go unnoticed.
 */
public final class HacBlockIndex {
    private static final Map<Set<?>, HacBlockIndex> INDEXES = Collections.synchronizedMap(new IdentityHashMap<>());

    private final int size;
    private final Map<Block, List<BlockSet>> byBlock = new IdentityHashMap<>();

    private HacBlockIndex(Set<BlockSet> set) {
        size = set.size();
        for (BlockSet b : set) byBlock.computeIfAbsent(b.block, k -> new ArrayList<>(1)).add(b);
    }

    /** @throws java.util.ConcurrentModificationException if the set changes mid-rebuild (caller falls back). */
    public static BlockSet find(Set<BlockSet> set, BlockSet key) {
        HacBlockIndex idx = INDEXES.get(set);
        if (idx == null || idx.size != set.size()) INDEXES.put(set, idx = new HacBlockIndex(set));
        BlockSet found = null;
        List<BlockSet> same = idx.byBlock.get(key.block);
        if (same != null) for (BlockSet b : same)
            if (b.meta == 32767 || key.meta == 32767 || b.meta == key.meta) found = b; // no break: original keeps the LAST match
        return found;
    }
}
