package de.originalpvp.features;

import de.originalpvp.settings.ModSettings;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Smooth zoom feature that interpolates FOV when the zoom key is held and supports
 * mouse wheel adjustment while zooming.
 */
public class Zoom implements IFeature {

    private static final float MIN_ZOOM_FOV = 10.0f;
    private static final float MAX_ZOOM_FOV = 110.0f;
    private static final float SCROLL_STEP = 5.0f;

    private final ModSettings settings;

    private boolean zooming = false;
    private boolean wasZooming = false;
    private boolean transitioningOut = false;

    private float currentZoomFov = -1.0f;
    private float transitionStartFov = -1.0f;
    private float dynamicTargetFov;
    private long transitionStartTime = 0L;

    public Zoom(ModSettings settings) {
        this.settings = settings;
        this.dynamicTargetFov = settings.zoomFov;
    }

    @Override
    public String getName() {
        return "Zoom";
    }

    @Override
    public boolean isEnabled() {
        return settings.zoomEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        settings.zoomEnabled = enabled;
        if (!enabled) {
            zooming = false;
            wasZooming = false;
            transitioningOut = false;
            currentZoomFov = -1.0f;
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        updateState(mc);
    }

    /**
     * Backward-compatible tick method.
     */
    public void onTick() {
        updateState(Minecraft.getMinecraft());
    }

    private void updateState(Minecraft mc) {
        boolean keyDown = mc.currentScreen == null
                && settings.zoomKeyCode > 0
                && Keyboard.isKeyDown(settings.zoomKeyCode);
        zooming = settings.zoomEnabled && keyDown;

        long now = System.currentTimeMillis();

        if (zooming && !wasZooming) {
            transitionStartTime = now;
            transitionStartFov = currentZoomFov; // -1 if not previously transitioning
            dynamicTargetFov = settings.zoomFov;
            transitioningOut = false;
        } else if (!zooming && wasZooming) {
            transitionStartTime = now;
            transitionStartFov = currentZoomFov;
            transitioningOut = true;
        }

        if (zooming) {
            int dWheel = Mouse.getDWheel();
            if (dWheel != 0) {
                dynamicTargetFov -= (dWheel > 0 ? SCROLL_STEP : -SCROLL_STEP);
                if (dynamicTargetFov < MIN_ZOOM_FOV) {
                    dynamicTargetFov = MIN_ZOOM_FOV;
                } else if (dynamicTargetFov > MAX_ZOOM_FOV) {
                    dynamicTargetFov = MAX_ZOOM_FOV;
                }
            }
        }

        wasZooming = zooming;
    }

    /**
     * @return {@code true} while the zoom key is actively held
     */
    public boolean isZooming() {
        return zooming;
    }

    /**
     * @return {@code true} if FOV should be modified (actively zooming or smoothly zooming out)
     */
    public boolean shouldModifyFov() {
        return zooming || transitioningOut;
    }

    /**
     * Computes the interpolated FOV for the current frame.
     */
    public float getZoomFov(float baseFov, float partialTicks) {
        if (!zooming && !transitioningOut) {
            currentZoomFov = -1.0f;
            return baseFov;
        }

        float startFov = transitionStartFov > 0.0f ? transitionStartFov : baseFov;
        float duration = Math.max(1.0f, settings.zoomSpeed);
        float elapsed = (float) (System.currentTimeMillis() - transitionStartTime);
        float progress = Math.min(1.0f, Math.max(0.0f, elapsed / duration));

        if (zooming) {
            currentZoomFov = startFov + (dynamicTargetFov - startFov) * progress;
        } else {
            currentZoomFov = startFov + (baseFov - startFov) * progress;
            if (progress >= 1.0f || Math.abs(currentZoomFov - baseFov) < 0.5f) {
                transitioningOut = false;
                currentZoomFov = -1.0f;
                return baseFov;
            }
        }

        return currentZoomFov;
    }
}
