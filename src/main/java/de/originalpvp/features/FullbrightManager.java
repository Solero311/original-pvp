package de.originalpvp.features;

import de.originalpvp.settings.ModSettings;
import net.minecraft.client.Minecraft;

/**
 * Manages fullbright (gamma override) while preserving the player's previous brightness setting.
 */
public class FullbrightManager implements IFeature {

    private static final float FULLBRIGHT_GAMMA = 100.0f;
    private static final float MAX_NORMAL_GAMMA = 10.0f;
    private static final float DEFAULT_GAMMA = 1.0f;

    private final ModSettings settings;
    private float previousGamma = DEFAULT_GAMMA;

    public FullbrightManager(ModSettings settings) {
        this.settings = settings;
    }

    @Override
    public String getName() {
        return "Fullbright";
    }

    @Override
    public boolean isEnabled() {
        return settings.fullbrightEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        settings.fullbrightEnabled = enabled;
    }

    @Override
    public void onInit(Minecraft mc) {
        if (mc != null && mc.gameSettings != null) {
            float current = mc.gameSettings.gammaSetting;
            if (current <= MAX_NORMAL_GAMMA && current >= 0.0f) {
                previousGamma = current;
            }
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc == null || mc.gameSettings == null) {
            return;
        }

        if (settings.fullbrightEnabled) {
            if (mc.gameSettings.gammaSetting < MAX_NORMAL_GAMMA) {
                previousGamma = mc.gameSettings.gammaSetting;
                mc.gameSettings.gammaSetting = FULLBRIGHT_GAMMA;
            }
        } else if (mc.gameSettings.gammaSetting > MAX_NORMAL_GAMMA) {
            mc.gameSettings.gammaSetting = (previousGamma >= 0.0f && previousGamma <= MAX_NORMAL_GAMMA)
                    ? previousGamma
                    : DEFAULT_GAMMA;
        }
    }
}
