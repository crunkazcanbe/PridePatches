package com.dogpound.patches.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.*;

/**
 * Untangles buttons other mods pile onto the inventory (her report 2026-09-28).
 *
 * WHY THEY STACK: Custom NPCs and Techguns both ship a copy of Galacticraft's inventory-tab API and EACH registers its
 * InitGuiEvent handler, so the shared tab list is added to the screen twice; LucraftCore sees that API and adds it a
 * third time. Same objects, same spot — you can't see it, and a click fires two or three times. Each of those mods
 * also brings its own "back to inventory" tab.
 *
 * FIX: (1) drop repeated references to the same button; (2) keep one tab per class and one "InventoryTabVanilla"
 * whichever mod it came from, then lay the tabs out left→right 28 px apart; (3) on the strip left of the inventory,
 * move any button that overlaps another straight down below it.
 */
public final class ButtonTidy {
    private ButtonTidy() {}

    private static java.lang.reflect.Field BUTTONS;

    @SuppressWarnings("unchecked")
    public static void tidy(GuiScreen gui) {
        if (!(gui instanceof GuiContainer)) return;
        List<GuiButton> list;
        try {
            if (BUTTONS == null) { BUTTONS = ObfuscationReflectionHelper.findField(GuiScreen.class, "field_146292_n"); }
            list = (List<GuiButton>) BUTTONS.get(gui); // looked up once: this now runs every frame
        } catch (Throwable t) { return; }
        if (list == null || list.size() < 2) return;

        // (1) the same button added more than once
        Set<GuiButton> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        int before = list.size();
        list.removeIf(b -> !seen.add(b));
        if (list.size() != before) log(gui, "removed " + (before - list.size()) + " repeated button(s)");

        // (2) inventory tabs from the Galacticraft tab API (and copies of it): one of each, side by side
        List<GuiButton> tabs = new ArrayList<>();
        for (GuiButton b : list) if (isTab(b)) tabs.add(b);
        if (tabs.size() > 1) {
            Set<String> kinds = new HashSet<>();
            List<GuiButton> keep = new ArrayList<>();
            for (GuiButton t : tabs) {
                String k = t.getClass().getSimpleName().equals("InventoryTabVanilla") ? "InventoryTabVanilla" : t.getClass().getName();
                if (kinds.add(k)) keep.add(t); else { list.remove(t); log(gui, "removed a second " + t.getClass().getName() + " tab"); }
            }
            keep.sort(Comparator.comparingInt(b -> b.x));
            int x0 = keep.get(0).x, y0 = keep.get(0).y;
            for (int i = 0; i < keep.size(); i++) {
                if (keep.get(i).x != x0 + i * 28) log(gui, "tab " + keep.get(i).getClass().getName() + " " + keep.get(i).x + " -> " + (x0 + i * 28));
                keep.get(i).x = x0 + i * 28; keep.get(i).y = y0;
            }
        }

        // (3) survival + creative inventory: every left-side button into one scrolling column (SideDock)
        boolean docked = SideDock.applies(gui);
        if (docked) SideDock.layout((GuiContainer) gui, list);

        // (3b) other screens, the strip left of the window: nothing may sit on top of anything else
        int guiLeft = ((GuiContainer) gui).getGuiLeft();
        List<GuiButton> left = new ArrayList<>();
        if (!docked)
        for (GuiButton b : list)
            if (b.visible && !isTab(b) && b.x + b.width <= guiLeft + 1 && !b.getClass().getName().startsWith("com.dogpound.realmcoin")) left.add(b);
        left.sort(Comparator.<GuiButton>comparingInt(b -> b.y).thenComparingInt(b -> b.x));
        List<GuiButton> placed = new ArrayList<>();
        for (GuiButton b : left) {
            for (int guard = 0; guard < 50; guard++) {
                GuiButton hit = null;
                for (GuiButton p : placed) if (overlaps(b, p)) { hit = p; break; }
                if (hit == null) break;
                log(gui, "left strip: " + b.getClass().getName() + " was on " + hit.getClass().getName() + " — moved down");
                b.y = hit.y + hit.height + 2;
            }
            placed.add(b);
        }

        // (4) anywhere else: a button sitting on another one slides right until it's clear (later-added one moves)
        List<GuiButton> done = new ArrayList<>();
        for (GuiButton b : list) {
            if (!b.visible || isTab(b) || left.contains(b) || b.getClass().getName().startsWith("com.dogpound.realmcoin")) { done.add(b); continue; }
            if (docked && SideDock.isMember(gui, b)) { done.add(b); continue; }   // the dock placed it
            boolean westSide = b.x + b.width <= guiLeft + 1;                  // left of the window: slide further left
            for (int guard = 0; guard < 20; guard++) {
                GuiButton hit = null;
                for (GuiButton p : done) if (p.visible && !isTab(p) && overlaps(b, p)) { hit = p; break; }
                if (hit == null) break;
                log(gui, b.getClass().getName() + " (id " + b.id + ") was on " + hit.getClass().getName() + " (id " + hit.id + ") — moved " + (westSide ? "left" : "right"));
                b.x = westSide ? hit.x - b.width - 2 : hit.x + hit.width + 2;
            }
            done.add(b);
        }
    }

    private static final Set<String> told = new HashSet<>();

    /** each fix is logged once per game, so a player (or I) can see what was stacked */
    static void log(GuiScreen gui, String what) {
        String line = gui.getClass().getSimpleName() + ": " + what;
        if (told.add(line)) org.apache.logging.log4j.LogManager.getLogger("PridePatches").info("[ButtonTidy] " + line);
    }

    static boolean overlaps(GuiButton a, GuiButton b) {
        return a.x < b.x + b.width && b.x < a.x + a.width && a.y < b.y + b.height && b.y < a.y + a.height;
    }

    /** a tab from any copy of the tab API: its class (or a parent) is called AbstractTab */
    static boolean isTab(GuiButton b) {
        for (Class<?> c = b.getClass(); c != null && c != GuiButton.class; c = c.getSuperclass())
            if (c.getSimpleName().equals("AbstractTab")) return true;
        return false;
    }
}
