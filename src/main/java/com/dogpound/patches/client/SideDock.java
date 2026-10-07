package com.dogpound.patches.client;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.lwjgl.input.Mouse;

/**
 * Requested: every button any mod puts on the LEFT of the survival/creative inventory lines up in one neat
 * column flush against the inventory, whatever mod it's from — no per-mod code. More than fit? ▲ ▼ arrows (and the
 * mouse wheel over the column) scroll through them. Buttons scrolled out of view are hidden (can't be clicked);
 * a button its own mod hides stays hidden and takes no room.
 * Limit: a tab a mod only DRAWS (no real button behind it) can't be moved from here.
 */
public final class SideDock {
    private SideDock() {}

    static { System.setProperty("pridepatches.sidedock", "on"); } // RealmCoin reads this and stops placing its own column

    private static final int GAP = 2, ARROW = 10, ARROW_W = 16, SIZE = 18;        // every button in the column: 18x18
    private static final Map<GuiScreen, Dock> DOCKS = new WeakHashMap<>();
    private static int savedScroll;                                 // same spot next time the inventory opens

    static final class Dock {
        final List<GuiButton> members = new ArrayList<>();
        final Set<GuiButton> hiddenByUs = Collections.newSetFromMap(new IdentityHashMap<>());
        final Set<GuiButton> skinned = Collections.newSetFromMap(new IdentityHashMap<>());
        boolean arrows, tabsAdded;
        int scroll = savedScroll, maxScroll, top, bottom, left, colW = 16;
    }

    /** buttons requested to have removed: Custom NPCs' two creative-inventory tabs (ids 150/151) */
    public static boolean suppressed(GuiButton b) {
        if (b.id != 150 && b.id != 151) return false;
        if (!b.getClass().getName().equals("noppes.npcs.client.gui.util.GuiNpcButton")) return false;
        return Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative;
    }

    public static boolean isMember(GuiScreen g, GuiButton b) { Dock d = DOCKS.get(g); return d != null && d.members.contains(b); }

    public static boolean applies(GuiScreen g) { return g instanceof GuiInventory || g instanceof GuiContainerCreative; }

    /** a fresh button list (init / resize): start over */
    public static void reset(GuiScreen g) { DOCKS.remove(g); }

    /** every frame, right before buttons draw */
    public static void layout(GuiContainer g, List<GuiButton> list) {
        Dock d = DOCKS.computeIfAbsent(g, k -> new Dock());
        if (!d.tabsAdded) { d.tabsAdded = true; if (g instanceof GuiContainerCreative) TabProxy.addAll(g, list); }
        int guiLeft = g.getGuiLeft(), guiTop = g.getGuiTop(), h = g.getYSize();
        d.members.removeIf(b -> !list.contains(b));
        List<GuiButton> fresh = new ArrayList<>();
        for (GuiButton b : list)
            if (!d.members.contains(b) && !d.skinned.contains(b) && !suppressed(b) && (b instanceof TabProxy                     // ours: parked off-screen until placed here
                    || b.visible && b.width > 0 && b.height > 0 && !ButtonTidy.isTab(b)
                    && b.x < guiLeft && b.x + b.width <= guiLeft + 8          // left of the inventory (tabs tuck a little under it)
                    && b.y < guiTop + h - 40                                   // not the hotbar row: those sit beside the hotbar on purpose
                    && b.width <= 32 && b.height <= 32 && b.x > 2 && b.y > 2        // icon/tab-sized only; never screen-edge UI (FTB's sidebar)
                    && !b.getClass().getName().startsWith("com.dogpound.prideinventory")))
                fresh.add(b);
        // each mod's buttons stay together (in the order that mod placed them); mods in the order they first appear
        // her order: real tabs first (Custom NPCs), then the tab-system mods under them (Techguns, Factions, Lucraft...),
        // then small icon buttons (RealmCoin, PridePrism...); inside a group, the order its mod placed them
        Map<String, Integer> modOrder = new HashMap<>();
        for (GuiButton b : fresh) modOrder.merge(modKey(b), b.y, Math::min);
        fresh.sort(Comparator.<GuiButton>comparingInt(SideDock::rank).thenComparingInt(b -> modOrder.get(modKey(b))).thenComparing(SideDock::modKey)
            .thenComparingInt(b -> b.y).thenComparingInt(b -> -b.x));
        for (GuiButton b : fresh) {
            if (b instanceof TabProxy || b instanceof Skin || b.getClass().getName().startsWith("com.dogpound.")) d.members.add(b);
            else {                                              // another mod's button: our look, its icon, its click
                Skin sk = new Skin(0x5EB0 + d.members.size(), b, g);
                d.skinned.add(b);
                list.add(sk);
                d.members.add(sk);
            }
        }
        for (GuiButton o : d.skinned) { o.x = -10000; o.y = -10000; o.visible = false; }   // the originals never draw

        for (GuiButton b : d.members) if (b instanceof TabProxy || b instanceof Skin || b.getClass().getName().startsWith("com.dogpound.")) { b.width = SIZE; b.height = SIZE; }
        List<GuiButton> shown = new ArrayList<>();
        for (GuiButton b : d.members) if (b.visible || d.hiddenByUs.contains(b)) shown.add(b);
        int total = -GAP;
        d.colW = 16;
        for (GuiButton b : shown) { total += b.height + GAP; d.colW = Math.max(d.colW, b.width); }
        d.left = guiLeft;
        // room: exactly the inventory window's height, never bigger (requested feature)
        int roomTop = Math.max(0, guiTop), roomBottom = Math.min(g.height, guiTop + h);
        d.arrows = total > roomBottom - roomTop - 6;
        d.top = roomTop + (d.arrows ? ARROW + GAP + 3 : 3);
        d.bottom = roomBottom - (d.arrows ? ARROW + GAP + 3 : 3);
        // the furthest you can scroll: the last page still starts early enough to fill the column
        int room = d.bottom - d.top, fit = 0, used = -GAP;
        for (int i = shown.size() - 1; i >= 0 && used + GAP + shown.get(i).height <= room; i--) { used += GAP + shown.get(i).height; fit++; }
        d.maxScroll = d.arrows ? Math.max(0, shown.size() - Math.max(1, fit)) : 0;
        d.scroll = Math.max(0, Math.min(d.scroll, d.maxScroll));
        savedScroll = d.scroll;

        if (!shown.isEmpty()) {                                              // one tidy panel behind them all
            int px0 = guiLeft - d.colW - 6, px1 = guiLeft - 1;
            int py0 = d.arrows ? d.top - ARROW - GAP - 3 : d.top - 3;
            int py1 = d.arrows ? d.bottom + GAP + ARROW + 3 : Math.min(d.bottom, d.top + total) + 3;
            Gui.drawRect(px0, py0, px1, py1, 0xC0140E22);
            Gui.drawRect(px0, py0, px1, py0 + 1, 0xFF5BCEFA);
            Gui.drawRect(px0, py1 - 1, px1, py1, 0xFFF5A9B8);
            Gui.drawRect(px0, py0, px0 + 1, py1, 0x805BCEFA);
        }
        int y = d.top;
        for (int i = 0; i < shown.size(); i++) {
            GuiButton b = shown.get(i);
            boolean on = i >= d.scroll && y + b.height <= d.bottom + (d.arrows ? 0 : 1000);
            if (on) {
                b.x = guiLeft - 3 - d.colW + (d.colW - b.width) / 2;          // centred in one even column
                b.y = y;
                y += b.height + GAP;
                if (d.hiddenByUs.remove(b)) b.visible = true;
            } else if (b.visible) { b.visible = false; d.hiddenByUs.add(b); }
        }
    }

    /** 0 = a real tab (big), 1 = a tab-system mod's button, 2 = a small icon button */
    private static int rank(GuiButton b) {
        if (b instanceof Skin) return rank(((Skin) b).orig);
        if (b instanceof TabProxy) return 1;
        return b.width >= 24 && b.height >= 24 ? 0 : 2;
    }

    /** which mod a button belongs to (its package, two levels) — tab-system proxies group as one "mod tabs" set */
    private static String modKey(GuiButton b) {
        if (b instanceof Skin) return modKey(((Skin) b).orig);
        if (b instanceof TabProxy) return "~tabs";
        String n = b.getClass().getName();
        String[] p = n.split("\\.");
        return p.length > 2 ? p[0] + "." + p[1] + "." + p[2] : n;
    }

    /** after the buttons: the arrows, and the name of a mod tab you point at */
    public static void drawArrows(GuiScreen g, int mx, int my) {
        Dock d = DOCKS.get(g);
        if (d == null) return;
        for (GuiButton b : d.members)
            if ((b instanceof TabProxy || b instanceof Skin) && b.visible && mx >= b.x && my >= b.y && mx < b.x + b.width && my < b.y + b.height)
                g.drawHoveringText(java.util.Collections.singletonList(b instanceof TabProxy ? ((TabProxy) b).name : ((Skin) b).name), mx, my);
        if (!d.arrows) return;
        int x = d.left - 3 - d.colW + (d.colW - ARROW_W) / 2, upY = d.top - ARROW - GAP, downY = d.bottom + GAP;
        arrow(g, x, upY, true, d.scroll > 0, mx, my);
        arrow(g, x, downY, false, d.scroll < d.maxScroll, mx, my);
    }

    private static void arrow(GuiScreen g, int x, int y, boolean up, boolean can, int mx, int my) {
        boolean over = can && mx >= x && mx < x + ARROW_W && my >= y && my < y + ARROW;
        Gui.drawRect(x, y, x + ARROW_W, y + ARROW, over ? 0xE03A2D55 : 0xD01C1530);
        int edge = !can ? 0xFF555555 : over ? 0xFFF5A9B8 : 0xFF5BCEFA;
        Gui.drawRect(x, y, x + ARROW_W, y + 1, edge); Gui.drawRect(x, y + ARROW - 1, x + ARROW_W, y + ARROW, edge);
        Gui.drawRect(x, y, x + 1, y + ARROW, edge); Gui.drawRect(x + ARROW_W - 1, y, x + ARROW_W, y + ARROW, edge);
        int c = can ? 0xFFFFFFFF : 0xFF777777, cx = x + ARROW_W / 2;
        for (int r = 0; r < 4; r++) {                                   // a little triangle
            int row = up ? y + 3 + r : y + ARROW - 4 - r;
            Gui.drawRect(cx - r - 1, row, cx + r + 1, row + 1, c);
        }
    }

    /** true = the dock used this mouse event (arrow click or wheel over the column) */
    public static boolean mouse(GuiScreen g) {
        Dock d = DOCKS.get(g);
        if (d == null || !d.arrows) return false;
        Minecraft mc = Minecraft.getMinecraft();
        int mx = Mouse.getEventX() * g.width / mc.displayWidth, my = g.height - Mouse.getEventY() * g.height / mc.displayHeight - 1;
        int x = d.left - 3 - d.colW + (d.colW - ARROW_W) / 2;
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && mx < d.left && mx >= d.left - 48 && my >= d.top - ARROW - GAP && my < d.bottom + ARROW + GAP) {
            d.scroll += wheel > 0 ? -1 : 1;
            return true;
        }
        if (Mouse.getEventButton() != 0 || !Mouse.getEventButtonState() || mx < x || mx >= x + ARROW_W) return false;
        if (my >= d.top - ARROW - GAP && my < d.top - GAP) { d.scroll--; click(mc); return true; }
        if (my >= d.bottom + GAP && my < d.bottom + GAP + ARROW) { d.scroll++; click(mc); return true; }
        return false;
    }

    private static void click(Minecraft mc) {
        mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1f));
    }

    /**
     * Techguns, Custom NPCs (factions/quests), Lucraft... use the shared Galacticraft inventory-tab list, which only
     * ever goes on the SURVIVAL inventory. In creative, each of those tabs becomes a dock button with the mod's icon
     * that does exactly what the tab does.
     */
    static final class TabProxy extends GuiButton {
        final Object tab;
        final String name;
        final net.minecraft.item.ItemStack icon;

        TabProxy(int id, Object tab, String name, net.minecraft.item.ItemStack icon) {
            super(id, -100, -100, SIZE, SIZE, "");
            this.tab = tab; this.name = name; this.icon = icon;
        }

        static void addAll(GuiScreen g, List<GuiButton> list) {
            List<?> tabs;
            try { tabs = (List<?>) Class.forName("micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry").getMethod("getTabList").invoke(null); }
            catch (Throwable t) { return; }                       // no mod with inventory tabs
            int id = 0x7AB0;
            for (Object tab : tabs) {
                try {
                    if (tab.getClass().getSimpleName().equals("InventoryTabVanilla")) continue;   // "back to inventory": we're already there
                    if (!(Boolean) tab.getClass().getMethod("shouldAddToList").invoke(tab)) continue;
                    net.minecraft.item.ItemStack icon = net.minecraft.item.ItemStack.EMPTY;
                    for (Class<?> c = tab.getClass(); c != null && icon.isEmpty(); c = c.getSuperclass())
                        for (java.lang.reflect.Field f : c.getDeclaredFields())
                            if (f.getType() == net.minecraft.item.ItemStack.class) { f.setAccessible(true); Object v = f.get(tab); if (v != null) icon = (net.minecraft.item.ItemStack) v; break; }
                    list.add(new TabProxy(id++, tab, nice(tab), icon));
                } catch (Throwable ignored) {}
            }
        }

        /** "techguns.gui.player.tabs.TGPlayerTab" -> "Techguns: TG Player" */
        private static String nice(Object tab) {
            String pkg = tab.getClass().getName();
            String[] parts = pkg.split("\\.");
            String mod = parts.length > 1 && (parts[0].equals("com") || parts[0].equals("net") || parts[0].equals("org") || parts[0].equals("micdoodle8") || parts[0].equals("lucraft")) ? parts[parts[0].equals("lucraft") ? 2 : 1] : parts[0];
            if (mod.equals("mods") && parts.length > 2) mod = parts[2];
            String cls = tab.getClass().getSimpleName();
            if (cls.contains("$")) cls = cls.substring(cls.lastIndexOf('$') + 1);
            cls = cls.replace("InventoryTab", "").replace("Tab", "").replaceAll("([a-z])([A-Z])", "$1 $2");
            return Character.toUpperCase(mod.charAt(0)) + mod.substring(1) + (cls.isEmpty() ? "" : ": " + cls);
        }

        @Override
        public void drawButton(Minecraft mc, int mx, int my, float pt) {
            if (!visible) return;
            hovered = mx >= x && my >= y && mx < x + width && my < y + height;
            drawRect(x, y, x + width, y + height, hovered ? 0xE03A2D55 : 0xD01C1530);
            int edge = hovered ? 0xFFF5A9B8 : 0xFF5BCEFA;
            drawRect(x, y, x + width, y + 1, edge); drawRect(x, y + height - 1, x + width, y + height, edge);
            drawRect(x, y, x + 1, y + height, edge); drawRect(x + width - 1, y, x + width, y + height, edge);
            if (!icon.isEmpty()) {
                net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(icon, x + 1, y + 1);
                net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
            } else mc.fontRenderer.drawStringWithShadow(name.substring(0, 1), x + 7, y + 6, 0xFFFFFFFF);
        }

        /** do what the tab does; return false so the creative screen itself never sees the click */
        @Override
        public boolean mousePressed(Minecraft mc, int mx, int my) {
            if (!visible || !enabled || mx < x || my < y || mx >= x + width || my >= y + height) return false;
            playPressSound(mc.getSoundHandler());
            try { tab.getClass().getMethod("onTabClicked").invoke(tab); }
            catch (Throwable t) { ButtonTidy.log(mc.currentScreen, "tab " + name + " failed: " + t); }
            return false;
        }
    }

    /**
     * Requested: every button in the column must look identical. Another mod's button (e.g. Custom NPCs' big grey tabs)
     * is drawn inside our frame, scaled to fit; clicking it sends exactly what the game would have sent for the
     * original (Forge's ActionPerformed events + the screen's own handler), so the mod behaves as before.
     */
    static final class Skin extends GuiButton {
        final GuiButton orig;
        final GuiScreen screen;
        final String name;

        Skin(int id, GuiButton orig, GuiScreen screen) {
            super(id, -100, -100, SIZE, SIZE, "");
            this.orig = orig; this.screen = screen;
            String n = orig.getClass().getName(), mod = n.split("\\.").length > 1 ? n.split("\\.")[n.startsWith("com.") || n.startsWith("net.") ? 1 : 0] : n;
            String label = orig.displayString == null || orig.displayString.trim().isEmpty() ? "" : ": " + orig.displayString;
            this.name = Character.toUpperCase(mod.charAt(0)) + mod.substring(1) + label;
        }

        @Override
        public void drawButton(Minecraft mc, int mx, int my, float pt) {
            if (!visible) return;
            hovered = mx >= x && my >= y && mx < x + width && my < y + height;
            drawRect(x, y, x + width, y + height, hovered ? 0xE03A2D55 : 0xD01C1530);
            int edge = hovered ? 0xFFF5A9B8 : 0xFF5BCEFA;
            drawRect(x, y, x + width, y + 1, edge); drawRect(x, y + height - 1, x + width, y + height, edge);
            drawRect(x, y, x + 1, y + height, edge); drawRect(x + width - 1, y, x + width, y + height, edge);
            // the original, shrunk into the frame
            int ox = orig.x, oy = orig.y; boolean ov = orig.visible;
            float sc = Math.min((width - 2f) / Math.max(1, orig.width), (height - 2f) / Math.max(1, orig.height));
            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.translate(x + 1 + (width - 2 - orig.width * sc) / 2f, y + 1 + (height - 2 - orig.height * sc) / 2f, 0);
            net.minecraft.client.renderer.GlStateManager.scale(sc, sc, 1);
            orig.x = 0; orig.y = 0; orig.visible = true;
            try { orig.drawButton(mc, -1000, -1000, pt); } catch (Throwable ignored) {}
            orig.x = ox; orig.y = oy; orig.visible = ov;
            net.minecraft.client.renderer.GlStateManager.popMatrix();
            net.minecraft.client.renderer.GlStateManager.color(1, 1, 1, 1);
        }

        /** click = what the game does for the original: Pre event, the screen's handler, Post event */
        @Override
        public boolean mousePressed(Minecraft mc, int mx, int my) {
            if (!visible || mx < x || my < y || mx >= x + width || my >= y + height) return false;
            playPressSound(mc.getSoundHandler());
            try {
                java.util.List<GuiButton> list = net.minecraftforge.fml.common.ObfuscationReflectionHelper.getPrivateValue(GuiScreen.class, screen, "field_146292_n");
                net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre pre = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(screen, orig, list);
                if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(pre)) {
                    java.lang.reflect.Method m = net.minecraftforge.fml.common.ObfuscationReflectionHelper.findMethod(GuiScreen.class, "func_146284_a", void.class, GuiButton.class);
                    m.invoke(screen, pre.getButton());
                    if (screen.equals(mc.currentScreen))
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(screen, orig, list));
                }
            } catch (Throwable t) { ButtonTidy.log(screen, "clicking " + name + " failed: " + t); }
            return false;
        }
    }
}
