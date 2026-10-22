package de.originalpvp.features;

import de.originalpvp.settings.ModSettings;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Manages fog rendering overrides (All Fog, Terrain Fog, Water/Lava Fog, and Void Fog).
 */
public class FogManager implements IFeature {

    private static final float DISABLED_FOG_START = 99999.0f;
    private static final float DISABLED_FOG_END = 199999.0f;
    private static final float DISABLED_FOG_DENSITY = 0.0f;

    private final ModSettings settings;

    public FogManager(ModSettings settings) {
        this.settings = settings;
    }

    @Override
    public String getName() {
        return "Fog Control";
    }

    @Override
    public boolean isEnabled() {
        return shouldCancelFog();
    }

    @Override
    public void setEnabled(boolean enabled) {
        settings.disableAllFog = enabled;
    }

    /**
     * @return {@code true} if any fog suppression setting is enabled
     */
    public boolean shouldCancelFog() {
        return settings.disableAllFog
                || settings.disableTerrainFog
                || settings.disableVoidFog
                || settings.disableWaterFog;
    }

    /**
     * Evaluates the player's current environment and overrides OpenGL fog parameters
     * at the end of {@code EntityRenderer.setupFog} when applicable.
     */
    public void applyFogOverrides(Minecraft mc) {
        if (!shouldCancelFog()) {
            return;
        }

        if (shouldSuppressCurrentFog(mc)) {
            GlStateManager.setFogStart(DISABLED_FOG_START);
            GlStateManager.setFogEnd(DISABLED_FOG_END);
            GlStateManager.setFogDensity(DISABLED_FOG_DENSITY);
        }
    }

    private boolean shouldSuppressCurrentFog(Minecraft mc) {
        if (settings.disableAllFog) {
            return true;
        }
        if (mc == null || mc.thePlayer == null) {
            return false;
        }

        boolean inFluid = mc.thePlayer.isInsideOfMaterial(Material.water)
                || mc.thePlayer.isInsideOfMaterial(Material.lava);

        if (inFluid) {
            return settings.disableWaterFog;
        }

        if (settings.disableTerrainFog) {
            return true;
        }

        return settings.disableVoidFog && mc.thePlayer.posY < 32.0D;
    }
}
