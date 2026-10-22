package de.originalpvp.patcher;

/**
 * Centralized Notch obfuscation mappings for Minecraft 1.8.9 (MCP stable_22).
 * Documents all obfuscated class, method, and field identifiers used by ASM transformers.
 */
public final class VanillaMappings {

    // ── Mod Hook Target ──────────────────────────────────────────────────
    public static final String HOOK_CLASS = "de/originalpvp/OriginalPvP";
    public static final String GUI_MOD_MENU_CLASS = "de/originalpvp/gui/GuiModMenu";

    // ── net.minecraft.client.Minecraft (ave) ─────────────────────────────
    public static final String MINECRAFT_CLASS = "ave";
    public static final String MINECRAFT_START_GAME_NAME = "am";
    public static final String MINECRAFT_START_GAME_DESC = "()V";
    public static final String MINECRAFT_RUN_TICK_NAME = "s";
    public static final String MINECRAFT_RUN_TICK_DESC = "()V";
    public static final String MINECRAFT_DISPLAY_GUI_NAME = "a";
    public static final String MINECRAFT_DISPLAY_GUI_DESC = "(Laxu;)V";

    // ── net.minecraft.client.renderer.EntityRenderer (bfk) ───────────────
    public static final String ENTITY_RENDERER_CLASS = "bfk";
    public static final String ER_GET_FOV_MODIFIER_NAME = "a";
    public static final String ER_GET_FOV_MODIFIER_DESC = "(FZ)F";
    public static final String ER_ORIENT_CAMERA_NAME = "f";
    public static final String ER_ORIENT_CAMERA_DESC = "(F)V";
    public static final String ER_SETUP_FOG_NAME = "a";
    public static final String ER_SETUP_FOG_DESC = "(IF)V";
    public static final String ER_UPDATE_CAMERA_AND_RENDER_NAME = "a";
    public static final String ER_UPDATE_CAMERA_AND_RENDER_DESC = "(FJ)V";

    // GameSettings.viewBobbing (avh.d : Z)
    public static final String GAME_SETTINGS_VIEW_BOBBING_FIELD = "d";
    public static final String BOOLEAN_DESC = "Z";

    // GuiIngame.renderGameOverlay (avo.a(F)V)
    public static final String GUI_INGAME_RENDER_OVERLAY_NAME = "a";
    public static final String GUI_INGAME_RENDER_OVERLAY_DESC = "(F)V";

    // ── net.minecraft.client.renderer.RenderGlobal (bfr) ─────────────────
    public static final String RENDER_GLOBAL_CLASS = "bfr";
    public static final String RG_RENDER_SKY_NAME = "a";
    public static final String RG_RENDER_SKY_DESC = "(FI)V";

    // ── net.minecraft.client.gui.GuiOptions (axn) & GuiScreen (axu) ──────
    public static final String GUI_OPTIONS_CLASS = "axn";
    public static final String GUI_SCREEN_CLASS = "axu";
    public static final String GUI_BUTTON_CLASS = "avs";

    public static final String GUI_INIT_NAME = "b";
    public static final String GUI_INIT_DESC = "()V";
    public static final String GUI_ACTION_PERFORMED_NAME = "a";
    public static final String GUI_ACTION_PERFORMED_DESC = "(Lavs;)V";

    public static final String GUI_SCREEN_BUTTON_LIST_FIELD = "n";
    public static final String GUI_SCREEN_WIDTH_FIELD = "l";
    public static final String GUI_SCREEN_HEIGHT_FIELD = "m";
    public static final String GUI_SCREEN_MC_FIELD = "j";
    public static final String GUI_BUTTON_ID_FIELD = "k";

    // ── net.minecraft.entity.player.InventoryPlayer (wm) ─────────────────
    public static final String INVENTORY_PLAYER_CLASS = "wm";
    public static final String IP_CHANGE_CURRENT_item_NAME = "d";
    public static final String IP_CHANGE_CURRENT_ITEM_DESC = "(I)V";

    // ── net.minecraft.client.gui.GuiOverlayDebug (avv) ───────────────────
    public static final String GUI_OVERLAY_DEBUG_CLASS = "avv";
    public static final String GOD_CALL_NAME = "b";
    public static final String GOD_CALL_DESC = "()Ljava/util/List;";

    private VanillaMappings() {
        // Prevent instantiation
    }
}
