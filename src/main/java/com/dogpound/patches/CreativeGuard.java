package com.dogpound.patches;

/**
 * Shared flag for the creative-inventory page-button guard.
 *
 * Set while a click is being dispatched inside GuiContainerCreative, cleared on the next frame.
 * MixinMinecraftCreativeGuard refuses to close the creative screen while it is set, so clicking
 * the tab page arrows can never boot you back to the game no matter which mod is misbehaving.
 */
public final class CreativeGuard {
    private CreativeGuard() {}

    /** True while a mouse click is in flight inside the creative inventory. */
    public static boolean inClick = false;

    /** Logged once so we still learn who tried to close it. */
    public static boolean reported = false;

    /**
     * Her report 2026-09-28: "none of the buttons work". Every inventory side-bar button opened its screen and
     * something closed it again in the same frame (log: GuiHub / NutritionGui, then two closes). The same screen
     * opened by its hotkey stays open, so the close rides on the inventory click. A screen opened from the
     * inventory can't be closed in its first quarter second; a person can't do that anyway.
     */
    public static Object openedFromInventory;
    public static long openedAt;
    public static boolean reportedInv = false;
    /** the screen opened from the inventory that's already been re-sized (once is enough) */
    public static Object resized;
}
