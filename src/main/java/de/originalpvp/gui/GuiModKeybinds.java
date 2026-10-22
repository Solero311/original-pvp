package de.originalpvp.gui;

import de.originalpvp.OriginalPvP;
import de.originalpvp.settings.ModSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * In-game configuration screen for rebinding Original PvP hotkeys.
 */
public class GuiModKeybinds extends GuiScreen {

    private static final int ID_DONE = 100;
    private static final int START_Y = 60;
    private static final int ROW_SPACING = 30;
    private static final int KEY_BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;

    private final GuiScreen parentScreen;
    private final List<KeybindEntry> keybindEntries = new ArrayList<KeybindEntry>();
    private int selectedKeybindId = -1;

    public GuiModKeybinds(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.keybindEntries.clear();
        this.selectedKeybindId = -1;

        final ModSettings s = OriginalPvP.getInstance().getSettings();

        registerKeybind(0, "Zoom Key:", () -> s.zoomKeyCode, code -> {
            s.zoomKeyCode = code;
            if (OriginalPvP.keyBindZoom != null) {
                OriginalPvP.keyBindZoom.setKeyCode(code);
            }
        });

        registerKeybind(1, "Toggle Sprint Key:", () -> s.toggleSprintKeyCode, code -> {
            s.toggleSprintKeyCode = code;
            if (OriginalPvP.keyBindSprintMod != null) {
                OriginalPvP.keyBindSprintMod.setKeyCode(code);
            }
        });

        registerKeybind(2, "Mod Menu Key:", () -> s.menuKeyCode, code -> {
            s.menuKeyCode = code;
            if (OriginalPvP.keyBindGui != null) {
                OriginalPvP.keyBindGui.setKeyCode(code);
            }
        });

        int buttonX = this.width / 2 - 40;
        for (int i = 0; i < keybindEntries.size(); i++) {
            KeybindEntry entry = keybindEntries.get(i);
            int y = START_Y + i * ROW_SPACING;
            this.buttonList.add(new GuiButton(entry.id, buttonX, y, KEY_BUTTON_WIDTH, BUTTON_HEIGHT, formatKeyName(entry.getter.getAsInt())));
        }

        this.buttonList.add(new GuiButton(ID_DONE, this.width / 2 - 100, this.height - 28, 200, BUTTON_HEIGHT, "Done"));
    }

    private void registerKeybind(int id, String label, IntSupplier getter, IntConsumer setter) {
        keybindEntries.add(new KeybindEntry(id, label, getter, setter));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == ID_DONE) {
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }

        for (int i = 0; i < keybindEntries.size(); i++) {
            if (keybindEntries.get(i).id == button.id) {
                this.selectedKeybindId = button.id;
                updateButtonTexts();
                return;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.selectedKeybindId >= 0) {
            if (keyCode != Keyboard.KEY_ESCAPE) {
                for (int i = 0; i < keybindEntries.size(); i++) {
                    KeybindEntry entry = keybindEntries.get(i);
                    if (entry.id == this.selectedKeybindId) {
                        entry.setter.accept(keyCode);
                        break;
                    }
                }
                KeyBinding.resetKeyBindingArrayAndHash();
                OriginalPvP.getInstance().getSettingsManager().save();
                this.mc.gameSettings.saveOptions();
            }

            this.selectedKeybindId = -1;
            updateButtonTexts();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private void updateButtonTexts() {
        for (int i = 0; i < this.buttonList.size(); i++) {
            GuiButton btn = this.buttonList.get(i);
            for (int j = 0; j < keybindEntries.size(); j++) {
                KeybindEntry entry = keybindEntries.get(j);
                if (btn.id == entry.id) {
                    String keyName = formatKeyName(entry.getter.getAsInt());
                    btn.displayString = (this.selectedKeybindId == entry.id)
                            ? "> " + keyName + " <"
                            : keyName;
                    break;
                }
            }
        }
    }

    private static String formatKeyName(int keyCode) {
        String name = Keyboard.getKeyName(keyCode);
        return name == null ? "KEY_" + keyCode : name;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Original PvP Keybinds", this.width / 2, 15, 0xFFFFFF);

        int labelX = this.width / 2 - 140;
        for (int i = 0; i < keybindEntries.size(); i++) {
            int labelY = START_Y + i * ROW_SPACING + 6;
            this.drawString(this.fontRendererObj, keybindEntries.get(i).label, labelX, labelY, 0xFFFFFF);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static final class KeybindEntry {
        final int id;
        final String label;
        final IntSupplier getter;
        final IntConsumer setter;

        KeybindEntry(int id, String label, IntSupplier getter, IntConsumer setter) {
            this.id = id;
            this.label = label;
            this.getter = getter;
            this.setter = setter;
        }
    }
}
