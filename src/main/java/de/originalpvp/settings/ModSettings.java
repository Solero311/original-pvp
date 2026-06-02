package de.originalpvp.settings;

/**
 * Simple POJO holding all mod settings with sensible defaults.
 */
public class ModSettings {

    // Zoom
    public boolean zoomEnabled = true;
    public int zoomKeyCode = 47;       // Keyboard.KEY_V
    public float zoomFov = 25.0f;
    public float zoomSpeed = 200.0f;   // milliseconds for zoom interpolation

    // Toggle Sprint
    public boolean toggleSprintEnabled = true;
    public int toggleSprintKeyCode = 19; // Keyboard.KEY_R

    // Fog Control
    public boolean disableAllFog = false;
    public boolean disableTerrainFog = false;
    public boolean disableVoidFog = false;
    public boolean disableWaterFog = false;

    // Sky
    public boolean renderSky = true;

    // ── view bobbing ─────────────────────────────────────────────────────
    public boolean viewBobbingEnabled = true;

    // ── fov effects ──────────────────────────────────────────────────────
    public boolean fovEffectsEnabled = true;

    // ── keybinds ─────────────────────────────────────────────────────────
    public int menuKeyCode = org.lwjgl.input.Keyboard.KEY_RSHIFT;

    // ── fullbright ───────────────────────────────────────────────────────
    public boolean fullbrightEnabled = false;
}
