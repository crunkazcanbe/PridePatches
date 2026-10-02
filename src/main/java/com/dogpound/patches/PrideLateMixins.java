package com.dogpound.patches;

import java.util.Collections;
import java.util.List;
import zone.rong.mixinbooter.ILateMixinLoader;

/**
 * Patches for NON-coremod mods (Better With Mods, More MekaSuit Modules...). Their classes are not on the
 * classpath when the early config is prepared, so an early mixin logs "target not found" and then poisons
 * the class when it loads. MixinBooter queues these after mods are discovered instead.
 */
public class PrideLateMixins implements ILateMixinLoader {
    @Override public List<String> getMixinConfigs() { return Collections.singletonList("dogpoundpatches.late.mixins.json"); }
}
