package de.originalpvp.util;

/**
 * Global metadata and constant definitions for Original PvP.
 */
public final class ModConstants {

    public static final String MOD_ID = "originalpvp";
    public static final String MOD_NAME = "Original PvP";
    public static final String MOD_VERSION = "2.0";
    public static final String FULL_DISPLAY_NAME = MOD_NAME + " " + MOD_VERSION;

    public static final String MINECRAFT_VERSION = "1.8.9";
    public static final String INSTALLER_VERSION_ID = MINECRAFT_VERSION + "-OriginalPvP";

    public static final String CONFIG_RELATIVE_PATH = "config/originalpvp.properties";
    public static final String KEYBIND_CATEGORY = MOD_NAME;

    public static final int OPTIONS_BUTTON_ID = 999;

    private ModConstants() {
        // Prevent instantiation
    }
}
