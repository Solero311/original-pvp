package de.originalpvp.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.lwjgl.input.Keyboard;
import de.originalpvp.settings.ModSettings;

/**
 * Toggle sprint feature that keeps the player sprinting when toggled on.
 */
public class ToggleSprint {

    private final ModSettings settings;
    private boolean toggled = true;
    private boolean keyWasDown = false;

    public ToggleSprint(ModSettings settings) {
        this.settings = settings;
        this.toggled = settings.toggleSprintEnabled;
    }

    public void onTick(Minecraft mc) {
        boolean keyDown = mc.currentScreen == null && Keyboard.isKeyDown(settings.toggleSprintKeyCode);
        if (keyDown && !keyWasDown) {
            toggled = !toggled;
        }
        keyWasDown = keyDown;

        if (!settings.toggleSprintEnabled || mc.thePlayer == null) {
            return;
        }

        // If toggled ON, or physically holding the key, force the vanilla sprint keybind true!
        // This lets vanilla handle the sneak delay and momentum natively.
        if (toggled || keyDown) {
            setVanillaSprintPressed(mc, true);
        } else {
            // Restore actual hardware state so it doesn't get artificially stuck on
            int vanillaCode = mc.gameSettings.keyBindSprint.getKeyCode();
            boolean physicalState = vanillaCode != 0 && Keyboard.isKeyDown(vanillaCode);
            setVanillaSprintPressed(mc, physicalState);
        }
    }

    private void setVanillaSprintPressed(Minecraft mc, boolean pressed) {
        try {
            // Find the only boolean field in KeyBinding which corresponds to 'pressed'
            // This bypasses any string obfuscation issues in the live game.
            for (java.lang.reflect.Field field : net.minecraft.client.settings.KeyBinding.class.getDeclaredFields()) {
                if (field.getType() == boolean.class) {
                    field.setAccessible(true);
                    field.setBoolean(mc.gameSettings.keyBindSprint, pressed);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isToggled() {
        return toggled;
    }

    public void toggle() {
        toggled = !toggled;
    }
}
