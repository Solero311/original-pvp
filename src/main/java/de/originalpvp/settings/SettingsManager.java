package de.originalpvp.settings;

import de.originalpvp.util.ModConstants;
import de.originalpvp.util.ModLogger;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * Loads and persists {@link ModSettings} to a properties file in the Minecraft configuration directory.
 */
public class SettingsManager {

    private static final String KEY_ZOOM_ENABLED = "zoom.enabled";
    private static final String KEY_ZOOM_KEY_CODE = "zoom.keyCode";
    private static final String KEY_ZOOM_FOV = "zoom.fov";
    private static final String KEY_ZOOM_SPEED = "zoom.speed";

    private static final String KEY_TOGGLE_SPRINT_ENABLED = "toggleSprint.enabled";
    private static final String KEY_TOGGLE_SPRINT_KEY_CODE = "toggleSprint.keyCode";

    private static final String KEY_FOG_DISABLE_ALL = "fog.disableAll";
    private static final String KEY_FOG_DISABLE_TERRAIN = "fog.disableTerrain";
    private static final String KEY_FOG_DISABLE_VOID = "fog.disableVoid";
    private static final String KEY_FOG_DISABLE_WATER = "fog.disableWater";

    private static final String KEY_SKY_RENDER = "sky.render";
    private static final String KEY_VIEW_BOBBING_ENABLED = "viewBobbing.enabled";
    private static final String KEY_FOV_EFFECTS_ENABLED = "fovEffects.enabled";
    private static final String KEY_FULLBRIGHT_ENABLED = "fullbright.enabled";

    private static final String KEY_MENU_KEY_CODE = "menu.keyCode";

    private final ModSettings settings;
    private final File customConfigFile;

    public SettingsManager(ModSettings settings) {
        this(settings, null);
    }

    public SettingsManager(ModSettings settings, File customConfigFile) {
        this.settings = settings;
        this.customConfigFile = customConfigFile;
    }

    private File resolveConfigFile() {
        if (customConfigFile != null) {
            return customConfigFile;
        }
        return new File(Minecraft.getMinecraft().mcDataDir, ModConstants.CONFIG_RELATIVE_PATH);
    }

    public void load() {
        File configFile = resolveConfigFile();
        if (!configFile.exists()) {
            save();
            return;
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
        } catch (IOException e) {
            ModLogger.error("Failed to read settings file: " + configFile.getAbsolutePath(), e);
            return;
        }

        settings.zoomEnabled = getBoolean(props, KEY_ZOOM_ENABLED, ModSettings.DEFAULT_ZOOM_ENABLED);
        settings.zoomKeyCode = getInt(props, KEY_ZOOM_KEY_CODE, ModSettings.DEFAULT_ZOOM_KEY_CODE);
        settings.zoomFov = getFloat(props, KEY_ZOOM_FOV, ModSettings.DEFAULT_ZOOM_FOV);
        settings.zoomSpeed = getFloat(props, KEY_ZOOM_SPEED, ModSettings.DEFAULT_ZOOM_SPEED);

        settings.toggleSprintEnabled = getBoolean(props, KEY_TOGGLE_SPRINT_ENABLED, ModSettings.DEFAULT_TOGGLE_SPRINT_ENABLED);
        settings.toggleSprintKeyCode = getInt(props, KEY_TOGGLE_SPRINT_KEY_CODE, ModSettings.DEFAULT_TOGGLE_SPRINT_KEY_CODE);

        settings.disableAllFog = getBoolean(props, KEY_FOG_DISABLE_ALL, ModSettings.DEFAULT_DISABLE_ALL_FOG);
        settings.disableTerrainFog = getBoolean(props, KEY_FOG_DISABLE_TERRAIN, ModSettings.DEFAULT_DISABLE_TERRAIN_FOG);
        settings.disableVoidFog = getBoolean(props, KEY_FOG_DISABLE_VOID, ModSettings.DEFAULT_DISABLE_VOID_FOG);
        settings.disableWaterFog = getBoolean(props, KEY_FOG_DISABLE_WATER, ModSettings.DEFAULT_DISABLE_WATER_FOG);

        settings.renderSky = getBoolean(props, KEY_SKY_RENDER, ModSettings.DEFAULT_RENDER_SKY);
        settings.viewBobbingEnabled = getBoolean(props, KEY_VIEW_BOBBING_ENABLED, ModSettings.DEFAULT_VIEW_BOBBING_ENABLED);
        settings.fovEffectsEnabled = getBoolean(props, KEY_FOV_EFFECTS_ENABLED, ModSettings.DEFAULT_FOV_EFFECTS_ENABLED);
        settings.fullbrightEnabled = getBoolean(props, KEY_FULLBRIGHT_ENABLED, ModSettings.DEFAULT_FULLBRIGHT_ENABLED);

        settings.menuKeyCode = getInt(props, KEY_MENU_KEY_CODE, ModSettings.DEFAULT_MENU_KEY_CODE);

        settings.validate();
    }

    public void save() {
        settings.validate();
        File configFile = resolveConfigFile();
        File parentDir = configFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        Properties props = new Properties();

        props.setProperty(KEY_ZOOM_ENABLED, Boolean.toString(settings.zoomEnabled));
        props.setProperty(KEY_ZOOM_KEY_CODE, Integer.toString(settings.zoomKeyCode));
        props.setProperty(KEY_ZOOM_FOV, Float.toString(settings.zoomFov));
        props.setProperty(KEY_ZOOM_SPEED, Float.toString(settings.zoomSpeed));

        props.setProperty(KEY_TOGGLE_SPRINT_ENABLED, Boolean.toString(settings.toggleSprintEnabled));
        props.setProperty(KEY_TOGGLE_SPRINT_KEY_CODE, Integer.toString(settings.toggleSprintKeyCode));

        props.setProperty(KEY_FOG_DISABLE_ALL, Boolean.toString(settings.disableAllFog));
        props.setProperty(KEY_FOG_DISABLE_TERRAIN, Boolean.toString(settings.disableTerrainFog));
        props.setProperty(KEY_FOG_DISABLE_VOID, Boolean.toString(settings.disableVoidFog));
        props.setProperty(KEY_FOG_DISABLE_WATER, Boolean.toString(settings.disableWaterFog));

        props.setProperty(KEY_SKY_RENDER, Boolean.toString(settings.renderSky));
        props.setProperty(KEY_VIEW_BOBBING_ENABLED, Boolean.toString(settings.viewBobbingEnabled));
        props.setProperty(KEY_FOV_EFFECTS_ENABLED, Boolean.toString(settings.fovEffectsEnabled));
        props.setProperty(KEY_FULLBRIGHT_ENABLED, Boolean.toString(settings.fullbrightEnabled));

        props.setProperty(KEY_MENU_KEY_CODE, Integer.toString(settings.menuKeyCode));

        File tempFile = new File(configFile.getParentFile(), configFile.getName() + ".tmp");
        try {
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                props.store(fos, "Original PvP Settings");
            }
            Files.move(tempFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            ModLogger.error("Failed to save settings to " + configFile.getAbsolutePath(), e);
        }
    }

    private static boolean getBoolean(Properties props, String key, boolean defaultValue) {
        String raw = props.getProperty(key);
        if (raw == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(raw.trim());
    }

    private static int getInt(Properties props, String key, int defaultValue) {
        String raw = props.getProperty(key);
        if (raw == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            ModLogger.warn("Invalid integer for setting '" + key + "': " + raw + " (using default " + defaultValue + ")");
            return defaultValue;
        }
    }

    private static float getFloat(Properties props, String key, float defaultValue) {
        String raw = props.getProperty(key);
        if (raw == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(raw.trim());
        } catch (NumberFormatException e) {
            ModLogger.warn("Invalid float for setting '" + key + "': " + raw + " (using default " + defaultValue + ")");
            return defaultValue;
        }
    }
}
