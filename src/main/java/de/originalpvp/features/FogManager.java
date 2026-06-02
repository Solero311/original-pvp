package de.originalpvp.features;

import de.originalpvp.settings.ModSettings;

/**
 * Manages fog rendering settings. Determines whether fog should be cancelled
 * based on current mod settings.
 */
public class FogManager {

    private final ModSettings settings;

    public FogManager(ModSettings settings) {
        this.settings = settings;
    }

    /**
     * Returns true if fog rendering should be cancelled.
     * Cancels if any individual fog setting is disabled or if the global
     * disable-all-fog flag is set.
     */
    public boolean shouldCancelFog() {
        return settings.disableAllFog
                || settings.disableTerrainFog
                || settings.disableVoidFog
                || settings.disableWaterFog;
    }
}
