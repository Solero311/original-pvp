package de.originalpvp.features;

import de.originalpvp.settings.ModSettings;
import net.minecraft.client.Minecraft;

/**
 * Manages dynamic FOV effect settings (sprinting, speed potions, bow draw, etc.).
 */
public class FovEffects implements IFeature {

    private final ModSettings settings;

    public FovEffects(ModSettings settings) {
        this.settings = settings;
    }

    @Override
    public String getName() {
        return "FOV Effects";
    }

    @Override
    public boolean isEnabled() {
        return settings.fovEffectsEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        settings.fovEffectsEnabled = enabled;
    }

    /**
     * @return {@code true} if dynamic FOV effects (sprinting, potions, etc.) should be applied
     */
    public boolean shouldApplyFovEffects() {
        return settings.fovEffectsEnabled;
    }

    /**
     * Returns either the dynamically modified FOV or the static user FOV setting
     * depending on whether FOV effects are enabled.
     */
    public float resolveFov(Minecraft mc, float dynamicFov) {
        if (settings.fovEffectsEnabled || mc == null || mc.gameSettings == null) {
            return dynamicFov;
        }
        return mc.gameSettings.fovSetting;
    }
}
