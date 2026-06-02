package de.originalpvp.features;

import de.originalpvp.settings.ModSettings;

/**
 * Manages sky rendering settings.
 */
public class SkyManager {

    private final ModSettings settings;

    public SkyManager(ModSettings settings) {
        this.settings = settings;
    }

    /**
     * Returns true if the sky should be rendered.
     */
    public boolean shouldRenderSky() {
        return settings.renderSky;
    }
}
