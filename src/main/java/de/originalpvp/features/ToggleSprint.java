package de.originalpvp.features;

import de.originalpvp.settings.ModSettings;
import de.originalpvp.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;

/**
 * Toggle sprint feature that keeps the player sprinting when toggled on,
 * delegating movement physics and sneak/hunger checks to vanilla Minecraft.
 */
public class ToggleSprint implements IFeature {

    private static final Field KEYBIND_PRESSED_FIELD = resolvePressedField();

    private final ModSettings settings;
    private boolean toggled;
    private boolean keyWasDown = false;
    private boolean wasForcingSprint = false;

    public ToggleSprint(ModSettings settings) {
        this.settings = settings;
        this.toggled = settings.toggleSprintEnabled;
    }

    private static Field resolvePressedField() {
        try {
            // KeyBinding in 1.8.9 has exactly one boolean field ('pressed' / 'h' in Notch mappings).
            // Resolving and caching it once avoids per-tick reflection overhead and obfuscation mismatches.
            for (Field field : KeyBinding.class.getDeclaredFields()) {
                if (field.getType() == boolean.class) {
                    field.setAccessible(true);
                    return field;
                }
            }
        } catch (Exception e) {
            ModLogger.error("Failed to cache KeyBinding.pressed field", e);
        }
        return null;
    }

    @Override
    public String getName() {
        return "Toggle Sprint";
    }

    @Override
    public boolean isEnabled() {
        return settings.toggleSprintEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        settings.toggleSprintEnabled = enabled;
    }

    @Override
    public void onTick(Minecraft mc) {
        boolean keyDown = mc.currentScreen == null
                && settings.toggleSprintKeyCode > 0
                && Keyboard.isKeyDown(settings.toggleSprintKeyCode);

        if (keyDown && !keyWasDown && settings.toggleSprintEnabled) {
            toggled = !toggled;
        }
        keyWasDown = keyDown;

        if (mc.thePlayer == null) {
            wasForcingSprint = false;
            return;
        }

        if (!settings.toggleSprintEnabled) {
            if (wasForcingSprint) {
                restorePhysicalSprintState(mc);
                wasForcingSprint = false;
            }
            return;
        }

        if (toggled || keyDown) {
            setVanillaSprintPressed(mc, true);
            wasForcingSprint = true;
        } else {
            restorePhysicalSprintState(mc);
            wasForcingSprint = false;
        }
    }

    private void restorePhysicalSprintState(Minecraft mc) {
        int vanillaCode = mc.gameSettings.keyBindSprint.getKeyCode();
        boolean physicalState = mc.currentScreen == null && vanillaCode > 0 && Keyboard.isKeyDown(vanillaCode);
        setVanillaSprintPressed(mc, physicalState);
    }

    private void setVanillaSprintPressed(Minecraft mc, boolean pressed) {
        if (KEYBIND_PRESSED_FIELD == null || mc.gameSettings == null || mc.gameSettings.keyBindSprint == null) {
            return;
        }
        try {
            KEYBIND_PRESSED_FIELD.setBoolean(mc.gameSettings.keyBindSprint, pressed);
        } catch (Exception e) {
            ModLogger.error("Failed to update sprint key state", e);
        }
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public void toggle() {
        toggled = !toggled;
    }
}
