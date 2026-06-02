package de.originalpvp.features;

import org.lwjgl.input.Keyboard;
import de.originalpvp.settings.ModSettings;

/**
 * Smooth zoom feature that interpolates FOV when the zoom key is held.
 */
public class Zoom {

    private final ModSettings settings;
    private boolean zooming = false;
    private float currentZoomFov;
    private long zoomStartTime = 0;
    private float zoomFromFov;
    private float dynamicTargetFov;
    private boolean wasZooming = false;

    public Zoom(ModSettings settings) {
        this.settings = settings;
        this.currentZoomFov = settings.zoomFov;
    }

    public void onTick() {
        boolean keyDown = net.minecraft.client.Minecraft.getMinecraft().currentScreen == null && Keyboard.isKeyDown(settings.zoomKeyCode);
        zooming = keyDown && settings.zoomEnabled;

        if (zooming && !wasZooming) {
            zoomStartTime = System.currentTimeMillis();
            zoomFromFov = currentZoomFov;
            dynamicTargetFov = settings.zoomFov; // reset to default
        }
        
        if (zooming) {
            int dWheel = org.lwjgl.input.Mouse.getDWheel();
            if (dWheel != 0) {
                // negative scroll = zoom out (increase FOV)
                // positive scroll = zoom in (decrease FOV)
                dynamicTargetFov -= (dWheel > 0 ? 5.0f : -5.0f);
                if (dynamicTargetFov < 10.0f) dynamicTargetFov = 10.0f;
                if (dynamicTargetFov > 110.0f) dynamicTargetFov = 110.0f;
            }
        }
        
        wasZooming = zooming;
    }

    public boolean isZooming() {
        return zooming;
    }

    public float getZoomFov(float currentFov, float partialTicks) {
        if (!zooming && currentZoomFov >= currentFov - 1.0f) {
            return currentFov;
        }

        float elapsed = (float) (System.currentTimeMillis() - zoomStartTime);
        float progress = Math.min(1.0f, elapsed / settings.zoomSpeed);

        if (zooming) {
            currentZoomFov = currentFov + (dynamicTargetFov - currentFov) * progress;
        } else {
            currentZoomFov = zoomFromFov + (currentFov - zoomFromFov) * progress;
        }

        return currentZoomFov;
    }
}
