package de.originalpvp.features;

import de.originalpvp.settings.ModSettings;

/**
 * Manages view bobbing settings.
 */
public class ViewBobbing {

    private final ModSettings settings;

    public ViewBobbing(ModSettings settings) {
        this.settings = settings;
    }

    /**
     * Returns true if view bobbing should be applied.
     */
    public boolean shouldApplyBobbing() {
        return settings.viewBobbingEnabled;
    }
}
