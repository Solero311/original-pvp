package de.originalpvp.features;

import de.originalpvp.settings.ModSettings;

/**
 * Manages camera view bobbing state.
 */
public class ViewBobbing implements IFeature {

    private final ModSettings settings;

    public ViewBobbing(ModSettings settings) {
        this.settings = settings;
    }

    @Override
    public String getName() {
        return "View Bobbing";
    }

    @Override
    public boolean isEnabled() {
        return settings.viewBobbingEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        settings.viewBobbingEnabled = enabled;
    }

    /**
     * @return {@code true} if view bobbing should be applied
     */
    public boolean shouldApplyBobbing() {
        return settings.viewBobbingEnabled;
    }
}
