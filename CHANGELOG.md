# Changelog

## 1.1.0 - 2026-10-07

### Added
- **Distant Horizons on Cleanroom:** registers Distant Horizons' mixin config directly, because Cleanroom never runs DH's `MixinConnector`. Without this, DH built LODs but never drew them.
- **Distant Horizons + Depths Update:** DH now uses the real world height (Y −64 to 320) and the real height of each chunk storage slot, so deep layers show up and LODs no longer float in the sky.
- **Structure template cache:** structure templates go through every mod's data fixes once, then the upgraded copy is saved in `config/pridepatches/template-cache/` and loaded on later launches. This removes minutes of world-creation stalls (for example More Creeps' castle).
- **Cascading worldgen trace:** when Forge reports cascading world generation, one stack trace per mod is written to the log file, so the exact generator can be identified.
- **Railcraft 12.1 beta names:** ships Railcraft 12.0's `en_us.lang` (plus a `pack.mcmeta` so Cleanroom loads it), so Railcraft items and the creative tab show real names instead of raw keys.
- **Unregistered tile entities:** a block entity class that was never registered gets a unique `pride_autofix:*` id instead of crashing the world when its chunk is saved or sent.

### Fixed
- **Biome crashes ("arraycopy: last source index 577 out of bounds for int[576]"):** Minecraft's shared `IntCache` scratch arrays are now per thread, so client biome checks, Distant Horizons' background LOD builder and custom biome layers no longer overwrite each other. Misaligned Voronoi zoom requests (BuildCraft's oil-biome replacer) are answered from the aligned area instead of reading past the array.
- **Slow structure loading:** data-fixer walkers no longer build a new `ResourceLocation` for every block entity. Vanilla's filtered walker and OpenMods compare the id string directly, and ProjectE and Ancient Warfare share one instance per id. One structure had held the server thread for over a minute.
- **World creation crashes:**
  - Industrial Upgrade ore veins below the bottom of a Depths Update world are skipped instead of crashing.
  - Open Terrain Generator worlds always treat dimension 0 as the overworld, fixing "getDimensionConfig(String) is null".
  - Clockwork Phase 2 leaves OTG's village, mineshaft, temple and stronghold generators alone (`ClassCastException`).
  - Better With Mods furnace tile entities left behind without their block are removed instead of crashing at "Preparing spawn area 99%".
  - RealWorld hanging plants no longer ask for a client player during world generation.
- **Slow new worlds with Gates of the Apocalypse:** the fortress search tests 16 spots within ±256 blocks instead of 1,000 spots within ±1,500 blocks, avoiding thousands of forced chunk generations (20+ minute loads).
- **Server lag in villages:** Villager Backport skips MCA villagers and waits 60 seconds after a home search that found nothing reachable (was up to 40% of the server thread).
- **Startup crashes:**
  - Duplicate kill/death statistic ids from two mobs with the same name are skipped instead of crashing.
  - Just Stargate's Ancient Shield armor material is kept when created, fixing "No enum constant ArmorMaterial.armor_shield".
  - Conquest Reforged no longer looks up item models before the item renderer exists (crash with Fossils & Archeology).
- **Client crashes:**
  - Opening the spectator teleport menu no longer crashes when another mod registered a plain texture at a skin location.
  - Serene Seasons' rain, snow and freeze helpers check their argument types first, fixing crashes in rain on Cleanroom.
  - GeoCraft walks a copy of its atmosphere map, fixing a server crash while many chunks load and unload.
- **Sky painted with block or vehicle textures:** 2D texturing is forced off before the sky renders, undoing a mod that enabled it behind `GlStateManager`'s back.
- **No game sound on Cleanroom with PolyPatcher:** PolyPatcher no longer restores an unsaved (−1) master volume at startup.
- **Slow startup with MCEF:** if the browser is already installed, MCEF uses its local config and checksum instead of waiting on its download mirror.

### Changed
- The Conquest Reforged and unregistered tile entity fixes now live in PridePatches, so all crash fixes ship in one jar.

