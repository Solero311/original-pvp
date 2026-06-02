package de.originalpvp.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import de.originalpvp.OriginalPvP;
import de.originalpvp.settings.ModSettings;

import java.io.IOException;

public class GuiModKeybinds extends GuiScreen {

    private final GuiScreen parentScreen;
    private int selectedKeybind = -1; // -1 = none, 0 = zoom, 1 = toggle sprint

    public GuiModKeybinds(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    private String getKeyName(int keyCode) {
        String name = Keyboard.getKeyName(keyCode);
        return name == null ? "KEY_" + keyCode : name;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        ModSettings s = OriginalPvP.getInstance().getSettings();

        // Zoom key button
        this.buttonList.add(new GuiButton(0, this.width / 2 - 40, 60, 120, 20, getKeyName(s.zoomKeyCode)));

        // Toggle sprint key button
        this.buttonList.add(new GuiButton(1, this.width / 2 - 40, 90, 120, 20, getKeyName(s.toggleSprintKeyCode)));

        // Menu key button
        this.buttonList.add(new GuiButton(2, this.width / 2 - 40, 120, 120, 20, getKeyName(s.menuKeyCode)));

        // Done button
        this.buttonList.add(new GuiButton(3, this.width / 2 - 100, this.height - 28, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.selectedKeybind = 0;
            button.displayString = "> " + getKeyName(OriginalPvP.getInstance().getSettings().zoomKeyCode) + " <";
        } else if (button.id == 1) {
            this.selectedKeybind = 1;
            button.displayString = "> " + getKeyName(OriginalPvP.getInstance().getSettings().toggleSprintKeyCode) + " <";
        } else if (button.id == 2) {
            this.selectedKeybind = 2;
            button.displayString = "> " + getKeyName(OriginalPvP.getInstance().getSettings().menuKeyCode) + " <";
        } else if (button.id == 3) {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.selectedKeybind >= 0) {
            ModSettings s = OriginalPvP.getInstance().getSettings();

            // Escape cancels the keybind selection
            if (keyCode == Keyboard.KEY_ESCAPE) {
                this.selectedKeybind = -1;
                updateButtonTexts();
                return;
            }

            if (this.selectedKeybind == 0) {
                s.zoomKeyCode = keyCode;
                if (OriginalPvP.keyBindZoom != null) {
                    OriginalPvP.keyBindZoom.setKeyCode(keyCode);
                }
            } else if (this.selectedKeybind == 1) {
                s.toggleSprintKeyCode = keyCode;
                if (OriginalPvP.keyBindSprintMod != null) {
                    OriginalPvP.keyBindSprintMod.setKeyCode(keyCode);
                }
            } else if (this.selectedKeybind == 2) {
                s.menuKeyCode = keyCode;
                // Also update the vanilla keybind so it works
                if (OriginalPvP.keyBindGui != null) {
                    OriginalPvP.keyBindGui.setKeyCode(keyCode);
                }
            }

            this.selectedKeybind = -1;
            OriginalPvP.getInstance().getSettingsManager().save();
            this.mc.gameSettings.saveOptions();
            updateButtonTexts();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private void updateButtonTexts() {
        ModSettings s = OriginalPvP.getInstance().getSettings();

        for (GuiButton btn : this.buttonList) {
            if (btn.id == 0) {
                btn.displayString = this.selectedKeybind == 0 ? "> ? <" : getKeyName(s.zoomKeyCode);
            }
            if (btn.id == 1) {
                btn.displayString = this.selectedKeybind == 1 ? "> ? <" : getKeyName(s.toggleSprintKeyCode);
            }
            if (btn.id == 2) {
                btn.displayString = this.selectedKeybind == 2 ? "> ? <" : getKeyName(s.menuKeyCode);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Original PvP Keybinds", this.width / 2, 15, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "Zoom Key:", this.width / 2 - 140, 66, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "Toggle Sprint Key:", this.width / 2 - 140, 96, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "Mod Menu Key:", this.width / 2 - 140, 126, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
