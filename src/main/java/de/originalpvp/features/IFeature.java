package de.originalpvp.features;

import net.minecraft.client.Minecraft;

/**
 * Contract for a modular Original PvP feature.
 */
public interface IFeature {

    /**
     * @return the human-readable name of this feature
     */
    String getName();

    /**
     * @return {@code true} if this feature is currently enabled in settings
     */
    boolean isEnabled();

    /**
     * Updates the enabled state of this feature in settings.
     */
    void setEnabled(boolean enabled);

    /**
     * Called once during mod initialization.
     */
    default void onInit(Minecraft mc) {
    }

    /**
     * Called once every client tick ({@code Minecraft.runTick()}).
     */
    default void onTick(Minecraft mc) {
    }
}
