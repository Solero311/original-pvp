package de.originalpvp.settings;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads and saves ModSettings to a .properties file in the Minecraft config directory.
 */
public class SettingsManager {

    private final ModSettings settings;

    public SettingsManager(ModSettings settings) {
        this.settings = settings;
    }

    private File getConfigFile() {
        return new File(Minecraft.getMinecraft().mcDataDir, "config/originalpvp.properties");
    }

    public void load() {
        File configFile = getConfigFile();
        if (!configFile.exists()) {
            save();
            return;
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            Properties props = new Properties();
            props.load(fis);

            settings.zoomEnabled = Boolean.parseBoolean(props.getProperty("zoom.enabled", "true"));
            settings.zoomKeyCode = Integer.parseInt(props.getProperty("zoom.keyCode", "47"));
            settings.zoomFov = Float.parseFloat(props.getProperty("zoom.fov", "10.0"));
            settings.zoomSpeed = Float.parseFloat(props.getProperty("zoom.speed", "200.0"));

            settings.toggleSprintEnabled = Boolean.parseBoolean(props.getProperty("toggleSprint.enabled", "true"));
            settings.toggleSprintKeyCode = Integer.parseInt(props.getProperty("toggleSprint.keyCode", "19"));

            settings.disableAllFog = Boolean.parseBoolean(props.getProperty("fog.disableAll", "false"));
            settings.disableTerrainFog = Boolean.parseBoolean(props.getProperty("fog.disableTerrain", "false"));
            settings.disableVoidFog = Boolean.parseBoolean(props.getProperty("fog.disableVoid", "false"));
            settings.disableWaterFog = Boolean.parseBoolean(props.getProperty("fog.disableWater", "false"));

            settings.renderSky = Boolean.parseBoolean(props.getProperty("sky.render", "true"));
            settings.viewBobbingEnabled = Boolean.parseBoolean(props.getProperty("viewBobbing.enabled", "true"));
            settings.fovEffectsEnabled = Boolean.parseBoolean(props.getProperty("fovEffects.enabled", "true"));
            
            settings.menuKeyCode = Integer.parseInt(props.getProperty("menu.keyCode", "54")); // 54 = RSHIFT
            settings.fullbrightEnabled = Boolean.parseBoolean(props.getProperty("fullbright.enabled", "false"));
        } catch (IOException | NumberFormatException e) {
            System.err.println("[OriginalPvP] Failed to load settings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void save() {
        File configFile = getConfigFile();
        configFile.getParentFile().mkdirs();

        Properties props = new Properties();

        props.setProperty("zoom.enabled", Boolean.toString(settings.zoomEnabled));
        props.setProperty("zoom.keyCode", Integer.toString(settings.zoomKeyCode));
        props.setProperty("zoom.fov", Float.toString(settings.zoomFov));
        props.setProperty("zoom.speed", Float.toString(settings.zoomSpeed));

        props.setProperty("toggleSprint.enabled", Boolean.toString(settings.toggleSprintEnabled));
        props.setProperty("toggleSprint.keyCode", Integer.toString(settings.toggleSprintKeyCode));

        props.setProperty("fog.disableAll", Boolean.toString(settings.disableAllFog));
        props.setProperty("fog.disableTerrain", Boolean.toString(settings.disableTerrainFog));
        props.setProperty("fog.disableVoid", Boolean.toString(settings.disableVoidFog));
        props.setProperty("fog.disableWater", Boolean.toString(settings.disableWaterFog));

        props.setProperty("sky.render", Boolean.toString(settings.renderSky));
        props.setProperty("viewBobbing.enabled", Boolean.toString(settings.viewBobbingEnabled));
        props.setProperty("fovEffects.enabled", Boolean.toString(settings.fovEffectsEnabled));
        
        props.setProperty("menu.keyCode", Integer.toString(settings.menuKeyCode));
        props.setProperty("fullbright.enabled", Boolean.toString(settings.fullbrightEnabled));

        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "Original PvP Settings");
        } catch (IOException e) {
            System.err.println("[OriginalPvP] Failed to save settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
