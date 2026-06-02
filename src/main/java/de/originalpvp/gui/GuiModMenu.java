package de.originalpvp.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import de.originalpvp.OriginalPvP;
import de.originalpvp.settings.ModSettings;

import java.io.IOException;

public class GuiModMenu extends GuiScreen {

    private final GuiScreen parentScreen;

    public GuiModMenu(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        ModSettings s = OriginalPvP.getInstance().getSettings();

        int leftX = this.width / 2 - 205;
        int rightX = this.width / 2 + 5;
        int btnW = 200;
        int btnH = 20;
        int startY = 40;
        int spacing = 24;

        // Left column
        this.buttonList.add(new GuiButton(0, leftX, startY, btnW, btnH,
                getToggleText("Zoom", s.zoomEnabled)));
        this.buttonList.add(new GuiButton(1, leftX, startY + spacing, btnW, btnH,
                getToggleText("Toggle Sprint", s.toggleSprintEnabled)));
        this.buttonList.add(new GuiButton(2, leftX, startY + spacing * 2, btnW, btnH,
                getToggleText("View Bobbing", this.mc.gameSettings.viewBobbing)));
        this.buttonList.add(new GuiButton(3, leftX, startY + spacing * 3, btnW, btnH,
                getToggleText("FOV Effects", s.fovEffectsEnabled)));
        this.buttonList.add(new GuiButton(4, leftX, startY + spacing * 4, btnW, btnH,
                getToggleText("Render Sky", s.renderSky)));

        // Right column
        this.buttonList.add(new GuiButton(5, rightX, startY, btnW, btnH,
                getToggleText("Disable All Fog", s.disableAllFog)));
        this.buttonList.add(new GuiButton(6, rightX, startY + spacing, btnW, btnH,
                getToggleText("Disable Terrain Fog", s.disableTerrainFog)));
        this.buttonList.add(new GuiButton(7, rightX, startY + spacing * 2, btnW, btnH,
                getToggleText("Disable Void Fog", s.disableVoidFog)));
        this.buttonList.add(new GuiButton(8, rightX, startY + spacing * 3, btnW, btnH,
                getToggleText("Disable Water Fog", s.disableWaterFog)));
        this.buttonList.add(new GuiButton(9, rightX, startY + spacing * 4, btnW, btnH,
                getToggleText("Fullbright", s.fullbrightEnabled)));

        // Bottom center buttons
        this.buttonList.add(new GuiButton(10, this.width / 2 - 100, this.height - 52, 200, 20, "Keybinds"));
        this.buttonList.add(new GuiButton(11, this.width / 2 - 100, this.height - 28, 200, 20, "Done"));
    }

    private String getToggleText(String label, boolean value) {
        return label + ": " + (value ? "\u00a7aON" : "\u00a7cOFF");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        ModSettings s = OriginalPvP.getInstance().getSettings();

        switch (button.id) {
            case 0:
                s.zoomEnabled = !s.zoomEnabled;
                break;
            case 1:
                s.toggleSprintEnabled = !s.toggleSprintEnabled;
                break;
            case 2:
                this.mc.gameSettings.viewBobbing = !this.mc.gameSettings.viewBobbing;
                this.mc.gameSettings.saveOptions();
                break;
            case 3:
                s.fovEffectsEnabled = !s.fovEffectsEnabled;
                break;
            case 4:
                s.renderSky = !s.renderSky;
                break;
            case 5:
                s.disableAllFog = !s.disableAllFog;
                break;
            case 6:
                s.disableTerrainFog = !s.disableTerrainFog;
                break;
            case 7:
                s.disableVoidFog = !s.disableVoidFog;
                break;
            case 8:
                s.disableWaterFog = !s.disableWaterFog;
                break;
            case 9:
                s.fullbrightEnabled = !s.fullbrightEnabled;
                break;
            case 10:
                this.mc.displayGuiScreen(new GuiModKeybinds(this));
                return;
            case 11:
                this.mc.displayGuiScreen(this.parentScreen);
                return;
            default:
                break;
        }

        OriginalPvP.getInstance().getSettingsManager().save();
        updateButtonTexts();
    }

    private void updateButtonTexts() {
        ModSettings s = OriginalPvP.getInstance().getSettings();

        for (GuiButton btn : this.buttonList) {
            switch (btn.id) {
                case 0:
                    btn.displayString = getToggleText("Zoom", s.zoomEnabled);
                    break;
                case 1:
                    btn.displayString = getToggleText("Toggle Sprint", s.toggleSprintEnabled);
                    break;
                case 2:
                    btn.displayString = getToggleText("View Bobbing", this.mc.gameSettings.viewBobbing);
                    break;
                case 3:
                    btn.displayString = getToggleText("FOV Effects", s.fovEffectsEnabled);
                    break;
                case 4:
                    btn.displayString = getToggleText("Render Sky", s.renderSky);
                    break;
                case 5:
                    btn.displayString = getToggleText("Disable All Fog", s.disableAllFog);
                    break;
                case 6:
                    btn.displayString = getToggleText("Disable Terrain Fog", s.disableTerrainFog);
                    break;
                case 7:
                    btn.displayString = getToggleText("Disable Void Fog", s.disableVoidFog);
                    break;
                case 8:
                    btn.displayString = getToggleText("Disable Water Fog", s.disableWaterFog);
                    break;
                case 9:
                    btn.displayString = getToggleText("Fullbright", s.fullbrightEnabled);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Original PvP Settings", this.width / 2, 15, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
