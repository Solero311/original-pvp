package de.originalpvp.settings;

/**
 * Configuration model holding all user-adjustable Original PvP settings and their defaults.
 */
public class ModSettings {

    // ── Default Constants ────────────────────────────────────────────────
    public static final boolean DEFAULT_ZOOM_ENABLED = true;
    public static final int DEFAULT_ZOOM_KEY_CODE = 47;          // Keyboard.KEY_V
    public static final float DEFAULT_ZOOM_FOV = 25.0f;
    public static final float DEFAULT_ZOOM_SPEED = 200.0f;       // ms

    public static final boolean DEFAULT_TOGGLE_SPRINT_ENABLED = true;
    public static final int DEFAULT_TOGGLE_SPRINT_KEY_CODE = 19; // Keyboard.KEY_R

    public static final boolean DEFAULT_DISABLE_ALL_FOG = false;
    public static final boolean DEFAULT_DISABLE_TERRAIN_FOG = false;
    public static final boolean DEFAULT_DISABLE_VOID_FOG = false;
    public static final boolean DEFAULT_DISABLE_WATER_FOG = false;

    public static final boolean DEFAULT_RENDER_SKY = true;
    public static final boolean DEFAULT_VIEW_BOBBING_ENABLED = true;
    public static final boolean DEFAULT_FOV_EFFECTS_ENABLED = true;
    public static final boolean DEFAULT_FULLBRIGHT_ENABLED = false;

    public static final int DEFAULT_MENU_KEY_CODE = 54;          // Keyboard.KEY_RSHIFT

    // ── Zoom Settings ────────────────────────────────────────────────────
    public boolean zoomEnabled = DEFAULT_ZOOM_ENABLED;
    public int zoomKeyCode = DEFAULT_ZOOM_KEY_CODE;
    public float zoomFov = DEFAULT_ZOOM_FOV;
    public float zoomSpeed = DEFAULT_ZOOM_SPEED;

    // ── Toggle Sprint Settings ───────────────────────────────────────────
    public boolean toggleSprintEnabled = DEFAULT_TOGGLE_SPRINT_ENABLED;
    public int toggleSprintKeyCode = DEFAULT_TOGGLE_SPRINT_KEY_CODE;

    // ── Fog Control Settings ─────────────────────────────────────────────
    public boolean disableAllFog = DEFAULT_DISABLE_ALL_FOG;
    public boolean disableTerrainFog = DEFAULT_DISABLE_TERRAIN_FOG;
    public boolean disableVoidFog = DEFAULT_DISABLE_VOID_FOG;
    public boolean disableWaterFog = DEFAULT_DISABLE_WATER_FOG;

    // ── Visual & World Settings ──────────────────────────────────────────
    public boolean renderSky = DEFAULT_RENDER_SKY;
    public boolean viewBobbingEnabled = DEFAULT_VIEW_BOBBING_ENABLED;
    public boolean fovEffectsEnabled = DEFAULT_FOV_EFFECTS_ENABLED;
    public boolean fullbrightEnabled = DEFAULT_FULLBRIGHT_ENABLED;

    // ── Keybind Settings ─────────────────────────────────────────────────
    public int menuKeyCode = DEFAULT_MENU_KEY_CODE;

    /**
     * Validates and clamps numeric settings to safe ranges.
     */
    public void validate() {
        if (zoomFov < 5.0f) {
            zoomFov = 5.0f;
        } else if (zoomFov > 110.0f) {
            zoomFov = 110.0f;
        }

        if (zoomSpeed < 1.0f) {
            zoomSpeed = 1.0f;
        } else if (zoomSpeed > 2000.0f) {
            zoomSpeed = 2000.0f;
        }
    }
}
