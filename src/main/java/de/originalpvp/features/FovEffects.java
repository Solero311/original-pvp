package de.originalpvp.features;

import de.originalpvp.settings.ModSettings;

/**
 * Manages FOV effect settings (speed-based FOV changes, potion effects, etc.).
 */
public class FovEffects {

    private final ModSettings settings;

    public FovEffects(ModSettings settings) {
        this.settings = settings;
    }

    /**
     * Returns true if FOV effects (sprinting, potion, etc.) should be applied.
     */
    public boolean shouldApplyFovEffects() {
        return settings.fovEffectsEnabled;
    }
}
