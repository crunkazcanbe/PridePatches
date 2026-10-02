# PridePatches

Runtime bug fixes for Minecraft 1.12.2 mods. It fixes crashes, freezes and slowdowns without touching any mod's jar.

## About

PridePatches is a single coremod that fixes bugs in other mods while the game runs. Each fix is a small Mixin patch aimed at one method in one mod. The original jars stay untouched, so you can update or remove those mods freely. If a mod isn't installed, its patches are skipped.

It was built for the Pride modpack, a 1.12.2 pack of about 750 mods running on Cleanroom. Every bug listed below happened in that pack: a crash report, a frozen world, an error flood in the log, or a profiler showing one mod eating the server thread. The fixes are general, so they work in any pack that has the affected mod.

Each fix aims to keep the mod's original behaviour and only remove the crash or the waste. Where something has to be skipped (a broken compat hook, a stale recipe entry), only that one piece is skipped and the rest of the mod loads normally.

The mod used to be called DogPoundPatches. That name still shows in log lines (`[DogPoundPatches]`) and in the internal mixin config names.

## Every bug it patches

Grouped by the mod the bug is in, in alphabetical order. **60 fixes in total.**

### AcademyCraft (LambdaLib2) — 2

| # | Bug | Fix |
|---|---|---|
| 1 | **Crash on world load on older GPUs.** LambdaLib2's `DebugDraw` compiles a developer-only debug shader the first time entities render. On older GPUs and drivers it fails to compile and throws "Error loading shader script" on every world load. | Cancels that debug render pass. It only draws developer debug spheres, which never appear in normal play. |
| 2 | **"must be static" on Cleanroom.** LambdaLib2 finds AcademyCraft's `@StateEventCallback` and registry callbacks on Scala `object` classes (e.g. `AbilityInterf$`) and rejects them because they aren't static. | Treats Scala object methods as static and calls them on the object's singleton (`MODULE$`). Patched in both the registry manager and its block/item event handler. |

### AE2 Wireless Universal Terminal (AE2WUT) — 1

| # | Bug | Fix |
|---|---|---|
| 3 | **Startup crash with Extended Crafting 1.5.6.** With more than 9 terminals combined, the all-in-one terminal recipe moves to an Extended Crafting table and calls `addShapeless(ItemStack, NonNullList)`, which no longer exists. | Registers the same recipe through the method that does exist, `addShapeless(ItemStack, Object...)`. |

### Better Weather — 2

| # | Bug | Fix |
|---|---|---|
| 4 | **The world generates itself outward every tick.** The lightning check looks at positions in neighbouring chunks, and `getRainHeight()` loads and generates any unloaded chunk it touches. Chunks at the edge of the loaded area kept the world generating outward (and running every mod's world generator) every tick: about 1/3 of the server thread in a fresh world. | On the server, a position in an unloaded chunk reports no rain height instead of generating the chunk. |
| 5 | **Lightning-rod check generates chunks.** It reads the block at a spot that may be in an unloaded chunk, which generates it (same problem as #4). | On the server, an unloaded chunk counts as "no rod". |

### Better With Mods — 2

| # | Bug | Fix |
|---|---|---|
| 6 | **Jumpy camera.** Hardcore Movement changes walk speed per block type and also rescales the field of view every time, so walking over mixed ground zooms the camera in and out. | Keeps the speed changes and drops the FOV change. |
| 7 | **Gloom warps the FOV.** Hardcore Gloom also warps the field of view in the dark. | Keeps the gloom and drops the warp. |

### Celeritas — 1

| # | Bug | Fix |
|---|---|---|
| 8 | **Texture stitch crash ("Applying mipmap").** Celeritas's mipmap code assumes every animated sprite has a first frame. Some sprites (e.g. one of Industrial Upgrade's quarry textures) have a null first frame, which crashes the whole texture stitch. Vanilla tolerates it. | Skips mipmap generation for just that sprite (it renders without mipmaps). The guard can never crash the stitch itself. |

### Chocolate Quest Repoured — 1

| # | Bug | Fix |
|---|---|---|
| 9 | **Crash on every quit.** CQR removes a world's dungeon generation manager on unload, but the world can get one more tick, finds no manager and crashes the server tick loop. | Skips the tick when the manager is already gone. |

### Custom NPCs (Unofficial) — 6

| # | Bug | Fix |
|---|---|---|
| 10 | **World won't start.** `ClientScriptData.clear()` calls `clear()` on a script that hasn't been created yet, which crashes in the integrated-server tick. | Skips the clear when there is no script yet (there is nothing to clear). |
| 11 | **Crash during world load.** The client tick reads the player (block reach and more) before the player exists. | Skips the Custom NPCs client tick until the player exists. |
| 12 | **20 error log entries per startup.** One mod's damage source has a null type, so `name.equals(...)` threw while Custom NPCs listed resistances. | A null name reads as "generic", which the same loop already skips. Same outcome, no exception. |
| 13 | **1,500+ error lines per startup, and more on every craft.** The shaped-recipe check loops over the trimmed width but indexes with the full width, reading past the end for recipes like the NPC wand and mob cloner. | Returns the same empty ingredient without throwing or logging. |
| 14 | **One CPU core pinned from launch until you join a world.** The version checker thread busy-waits with no sleep (46% of all CPU samples during load) only to post a chat message. | Skips the version checker. |
| 15 | **Creative tabs drawn over the inventory side dock.** Custom NPCs' two creative-inventory tabs (ids 150/151) use their own draw code and kept drawing on top of other buttons. | Removes those two tabs from the creative screen. Factions and Quests stay reachable from the survival inventory's tabs. |

### CyclopsCore — 1

| # | Bug | Fix |
|---|---|---|
| 16 | **"Tried registering X after its registration event" for every Cyclops mod** (CapabilityProxy, Integrated Dynamics/Tunnels, EvilCraft, ColossalChests...). CyclopsCore decides registration is over when any registry event fires, including another mod's early custom registry. | Always queues entries. Each is registered when its own registry's event fires. |

### Dimensional Doors — 1

| # | Bug | Fix |
|---|---|---|
| 17 | **Server crash during chunk generation.** A gateway schematic throws when its tile-entity data doesn't match the block that was placed, which kills the server tick loop. It was triggered via Mystcraft's background world profiling. | Skips that one gateway instead of crashing. |

### End Expansion — 1

| # | Bug | Fix |
|---|---|---|
| 18 | **15+ minute world start.** Every saved structure piece reloads its template and runs it through Forge's full data fixer (every mod's walkers over megabytes of NBT). All 257 templates are already in the current 1.12.2 format. | Reads current-format templates straight from the jar, once, without the fixer. Older or missing ones use the normal path. |

### Falling Leaves — 1

| # | Bug | Fix |
|---|---|---|
| 19 | **Crash when leaf particles spawn.** LoliASM's on-demand animated textures can leave a leaf sprite with zero frames. Falling Leaves reads frame 0 for the particle colour and crashes. | No frames is treated like no pixels: the particle colour is white. |

### Futurepack — 1

| # | Bug | Fix |
|---|---|---|
| 20 | **Random load crash in Dynamic Surroundings** ("Could not initialize class EnvironStateHandler$EnvironState"). Futurepack's airbrush recipe scanner thread starts mid-load and creates a fake client world. Dynamic Surroundings reacts to it before its biome registry exists and breaks for the rest of the session. It was a race, so it came and went. | Holds the scanner thread and starts it once the game has finished loading. |

### GregTech Food Option — 1

| # | Bug | Fix |
|---|---|---|
| 21 | **Crash during world join.** The chat filter checks the player's potion effects, but a chat message can arrive before the player exists. | Skips the filter while there is no player (there's nothing to filter yet). |

### HBM's Nuclear Tech Mod family (NTM CE, NTM Space, NTM Cursed / Leafia's Cursed Addon) — 8

| # | Bug | Fix |
|---|---|---|
| 22 | **2,754 error stack dumps during startup.** The in-game manual (QMAW) rebuilds on every resource reload. Reloads that other mods force during pre-init happen before any item exists, so every item icon fails. | Skips manual rebuilds until items are registered. The final reload after loading builds it with real items. |
| 23 | **Crash on join (NTM CE).** The player-sync packet handler checks the world but not the player and crashes when a packet arrives before the player spawns. | Skips the packet until the player exists. It is re-synced. |
| 24 | **Crash on join (NTM Space).** The space-radiation sync packet has the same missing player check. | Skips the packet until the player exists. It is re-synced. |
| 25 | **Startup crash: `NoSuchFieldError` in Leafia's Cursed Addon hazards.** NTM CE 2.5.0.4 removed the advanced-alloy items, but the addon still references `blades_advanced_alloy`. | Reads that removed item as air, so the addon loads and skips that one stale entry. |
| 26 | **Startup crash: `NoSuchFieldError` in Leafia's Cursed Addon assembler recipes.** It references the removed `coil_advanced_alloy`. | Reads it as air, so the other recipes register and only that stale recipe is skipped. |
| 27 | **World frozen at "Building terrain".** NTM Cursed's client passive effects (radiation, digamma, vignette) read the player's position before the player exists. | Skips the passive tick while there is no player. The digamma update is also guarded directly in case it is reached another way. |
| 28 | **Client crash when the saved player is in a space dimension.** A render frame can run before the view entity is set, and the acid-rain world render reads that null entity. | If the view entity is missing, points it at the player. If the player isn't ready either, skips only that frame's world pass. The GUI and loading screen still draw. |
| 29 | **Client crash in sky colour during the same load frame.** The acid-rain fog renderer asks the world for the sky colour with a null entity. | Returns black for that one frame (correct for space, invisible during loading). |

### Heat&Climate — 1

| # | Bug | Fix |
|---|---|---|
| 30 | **6% of the server thread.** Every block's heat, humidity and airflow lookup walks the whole registered set calling `equals()`. | Answers from a per-block index with identical rules: the last match wins, and meta 32767 is a wildcard. The index is rebuilt when the set's size changes. |

### Immersive Railroading (UniversalModCore) — 1

| # | Bug | Fix |
|---|---|---|
| 31 | **Crash on join:** "Called to get the player before minecraft has actually started!" A packet arrives before the player exists. | Skips applying the packet until the player exists. It is re-synced. |

### Industrial Upgrade, GregTech and other world generators — 1

| # | Bug | Fix |
|---|---|---|
| 32 | **World freezes when flying fast (cascading world generation).** During chunk population, mod world generators read and write blocks in neighbouring chunks that aren't loaded yet, which loads and generates those chunks in a chain. | While modded world generation runs, block writes, block reads and top-block lookups that target an unloaded chunk are refused (reads return air). The loaded 2×2 population area still works, so features still generate. |

### MineColonies — 1

| # | Bug | Fix |
|---|---|---|
| 33 | **Server tick crash in fuel discovery.** `ItemStorage.equals` compares NBT with `a.getTagCompound().equals(...)` after only checking that both aren't null, so an item without NBT compared with one that has NBT crashes. | Null-safe comparison with the same answers. |

### Minecraft / Forge / Cleanroom (core) — 18

| # | Bug | Fix |
|---|---|---|
| 34 | **Crash reports lost.** On modern Java, stack frames from lambdas and generated code have no file name. `CrashReportCategory` calls `.equals` on that null while building the report, so the real error is replaced by a different one and no crash report is written. | Compares against an empty string instead of null, so the report gets written and names the real cause. |
| 35 | **Every Scala mod fails on Cleanroom** with "Multiple entries with same key" (ProjectRed, OpenComputers, ForgeMultipart, MrTJPCore, bdlib, Gendustry...). Scala puts `@Mod` on both `Foo` and its companion `Foo$`, and Cleanroom builds a container for each. | Skips the `$` companion, matching classic Forge. |
| 36 | **One bad mixin config breaks other mods.** Cleanroom adds every jar's `MixinConfigs` in one call. A single bad entry (Embers 1.26.3 lists a config named just `embers`) aborts the rest, including other mods' late mixin loaders. | Adds the configs one at a time, logging and skipping broken ones. |
| 37 | **Silent freeze when a mod errors during Init.** Forge's `errorOccurred` looped forever on an error whose cause chain loops back on itself. Nothing was logged and no crash screen appeared. | Logs the mod and its error chain first (cycle-safe). If the chain loops, hands Forge a flat copy so it reaches its normal crash screen. |
| 38 | **World creation crash: "Attempted to modify LootTable after being finalized!"** Some mod's loot event handler edits an already-frozen table (The Midnight's fishing loot did). | Logs it once per table and ignores that change instead of crashing. |
| 39 | **"Save and Quit to Title" freezes forever on Cleanroom.** Closing the single-player network channel waits with no timeout, and the close never completes. The world is already saved at that point. | Waits at most 3 s, then continues the normal quit. Server shutdown and the final save still run. |
| 40 | **Frozen world on load (HUD).** The in-game HUD can render one frame before the player exists, which kills the render thread. | Skips the HUD for frames with no player. The loading screen still draws. |
| 41 | **Crash: "Updating screen events" during join.** When a screen closes, Minecraft checks the player's health to decide on the death screen, but the player can still be null (e.g. pressing a key on the black pre-spawn screen). | Treats a missing player as alive, so the requested screen is shown. |
| 42 | **Disconnect during join.** Inventory slot packets can be processed before the player is set. | Skips the slot update until the player exists. The server re-syncs the inventory. |
| 43 | **Freeze when mods ask for reach distance during load.** `getBlockReachDistance` reads the player's attributes. Mods that tick during load call it before the player exists. | Returns a normal default reach while there is no player. This fixes every caller at once. |
| 44 | **Crashes from entity packets during join.** Every entity packet (move, head look, velocity, metadata, spawn) goes through `getEntityByID`, which reads the player's id before the player exists. | Makes that one comparison null-safe, so all entity packet types are fixed at once. |
| 45 | **Crash from sound packets during join.** `playSound` measures the distance to the view entity, which can still be null. | Skips the sound when there is no listener yet. |
| 46 | **Random "Error executing task" crash on world load.** An integrated-server task reads the player's UUID before the client player has spawned. | Skips that task while there is no player. The player is registered through the normal join flow. |
| 47 | **Creative inventory closes when you turn a tab page.** Clicking the page arrows advanced the page and also closed the screen. | Blocks only a "close screen" call that happens during a click inside the creative inventory. Pressing E still closes it, and the offender is logged once. |
| 48 | **Inventory side-bar buttons close their screen instantly.** A screen opened from an inventory button was closed again in the same frame by the click. | Screens opened from the inventory can't be closed within their first 250 ms (no person can do that anyway). The cause is logged once. |
| 49 | **Screens opened from the inventory are squashed** when PolyPatcher's Inventory Scale is on. They are sized while the inventory's scale is active and then drawn at the normal scale. | Re-sizes the screen on the next tick, once it's the current screen. |
| 50 | **Inventory buttons stacked on top of each other.** Custom NPCs and Techguns each ship a copy of Galacticraft's inventory-tab API, and LucraftCore adds it again, so the same tabs are added two or three times in the same spot and one click fires several times. Each mod also brings its own "back to inventory" tab. | Removes duplicate buttons, keeps one tab of each kind, and lays the tabs out side by side. On other screens, overlapping buttons are moved apart. This is re-checked every frame, because some mods (Quark, Recipe Handler) move their buttons after the screen opens. |
| 51 | **Too many mod buttons left of the inventory.** | Every button that mods place on the left of the survival or creative inventory goes into one tidy column the height of the inventory. Scroll it with ▲ ▼ arrows or the mouse wheel. Hidden buttons can't be clicked. In creative, survival-only mod tabs (Techguns, Custom NPCs factions/quests, Lucraft...) appear as dock buttons that do the same thing. |

### More MekaSuit Modules — 1

| # | Bug | Fix |
|---|---|---|
| 52 | **Init crash: `ClassCastException`.** The mod casts the MekaSuit helmet to an AE2 wireless terminal, which only works in the Mekanism fork it was built for. | Skips only that registration. The rest of the mod loads. |

### normalasm / LoliASM — 1

| # | Bug | Fix |
|---|---|---|
| 53 | **Scrambled crash reports on Cleanroom.** The crash-report mod identifier calls `LaunchClassLoader.untransformName`, which doesn't exist on Cleanroom's class loader, so it throws while the report is being built. | Returns the class name unchanged, so the report finishes and names the real cause. |

### RealWorld — 1

| # | Bug | Fix |
|---|---|---|
| 54 | **New world stuck at "Preparing spawn area: 5%" for many minutes.** The cave moss/calcite code walks down while the block below is air. Below y=0 everything reads as air, so a column with nothing solid loops through billions of positions. | Nothing at or below y=0 counts as air, so the walk stops at the bottom of the world (all 3 methods). |

### StellarCore — 1

| # | Bug | Fix |
|---|---|---|
| 55 | **Freeze during world load.** HUD caching draws the overlay before its parent render event exists, which crashes on a null parent. | Skips the cached HUD while there is no player. |

### Thaumic Attempts — 1

| # | Bug | Fix |
|---|---|---|
| 56 | **11% of the server thread with zero anomaly wells in the world.** Every entity's collision box was scanned block by block looking for an anomaly well. | Tracks which wells exist and skips the scan unless one is loaded in that world. |

### Touhou Little Maid — 2

| # | Bug | Fix |
|---|---|---|
| 57 | **Server tick crash when creepers spawn.** To make creepers flee scarecrows, it casts the AI task set to `LinkedHashSet`, but a performance mod swaps that set for a concurrent one. | Adds the same behaviour (avoid scarecrows within 10 blocks, speed 1.0/1.2, priority 1, first in the list) on any kind of set. |
| 58 | **postInit crash with Custom NPCs Unofficial.** Its Custom NPCs compat uses `LayerHeadwear`, which only the official Custom NPCs has. | Skips that compat when the class is missing. Maids are unaffected. |

### Traincraft — 2

| # | Bug | Fix |
|---|---|---|
| 59 | **Init never finishes in a big pack (>20 min at 100% CPU).** Assembly Table recipes loop over whole ore lists nested inside each other and register one recipe per combination. | Inside that one method, each ore list yields only its first two entries (the vanilla ones). The Assembly Table keeps every recipe it had. |
| 60 | **Server time wasted on chunk unloads.** The chunk-unload handler looks up Jukebox Carts to mute their MP3 player, a client-only feature, but it also ran on the server for every unloaded chunk. | Skips it on the server, where no MP3 players exist. |

## Requirements

- Minecraft **1.12.2**
- **Cleanroom** (what the Pride pack uses), or **Forge 14.23.5** (built against 14.23.5.2860) with **MixinBooter**. Cleanroom includes MixinBooter.

Several fixes (#2, #35, #36, #39, #53) are for bugs that only happen on Cleanroom. On Forge they simply have nothing to do. None of the patched mods are required.

## Install

1. Install MixinBooter if you're on Forge (Cleanroom already has it).
2. Drop `PridePatches-1.12.2-<version>.jar` into your `mods/` folder.

## Config

There is no config file. Every patch targets one specific class. If the mod that class belongs to isn't installed, that patch is skipped and nothing else changes. Patches for regular (non-coremod) mods load late, after mods are discovered, so they can't break a class by loading too early. The patches log what they do with the prefixes `[DogPoundPatches]` and `PridePatches`.

## Building

```
./gradlew build
```

The jar ends up in `build/libs/`. The project targets Java 8 (build with a JDK 8). It compiles against the jars of the mods it patches, which aren't included. Put them in `libs/` first; `build.gradle` lists them. A few small stub classes in `src/main/java` exist only so the patches compile, and they are left out of the jar.

## License

MIT License, © 2026 crunkazcanbe.

## Credits

Made by crunkazcanbe, with Claude.


## Compile-only jars

The build compiles against these jars in `libs/` (other authors' mods / APIs). They are not included in this repo — get them from their official pages and drop them in `libs/` before building:

- `academycraft.jar`
- `ae2wut.jar`
- `betterweather.jar`
- `bwm.jar`
- `cnpc.jar`
- `cqr.jar`
- `cyclopscore.jar`
- `dimdoors.jar`
- `endexpansion.jar`
- `extendedcrafting.jar`
- `fallingleaves.jar`
- `hac.jar`
- `hbm.jar`
- `iu.jar`
- `minecolonies.jar`
- `mixinbooter-api.jar`
- `moremekasuit.jar`
- `realworld.jar`
- `sponge-mixin.jar`
- `thaumicattempts.jar`
- `touhou.jar`
- `traincraft.jar`
