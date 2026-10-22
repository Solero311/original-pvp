package de.originalpvp.features;

import de.originalpvp.settings.ModSettings;

/**
 * Manages sky rendering state.
 */
public class SkyManager implements IFeature {

    private final ModSettings settings;

    public SkyManager(ModSettings settings) {
        this.settings = settings;
    }

    @Override
    public String getName() {
        return "Render Sky";
    }

    @Override
    public boolean isEnabled() {
        return settings.renderSky;
    }

    @Override
    public void setEnabled(boolean enabled) {
        settings.renderSky = enabled;
    }

    /**
     * @return {@code true} if the sky should be rendered
     */
    public boolean shouldRenderSky() {
        return settings.renderSky;
    }
}
