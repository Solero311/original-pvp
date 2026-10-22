package de.originalpvp.gui;

import de.originalpvp.OriginalPvP;
import de.originalpvp.settings.ModSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * In-game configuration screen for toggling Original PvP features.
 */
public class GuiModMenu extends GuiScreen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int START_Y = 40;
    private static final int ROW_SPACING = 24;

    private static final int ID_KEYBINDS = 100;
    private static final int ID_DONE = 101;

    private final GuiScreen parentScreen;
    private final List<ToggleEntry> toggleEntries = new ArrayList<ToggleEntry>();

    public GuiModMenu(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.toggleEntries.clear();

        final ModSettings s = OriginalPvP.getInstance().getSettings();

        // Left Column (0)
        registerToggle(0, 0, 0, "Zoom", () -> s.zoomEnabled, () -> s.zoomEnabled = !s.zoomEnabled);
        registerToggle(1, 0, 1, "Toggle Sprint", () -> s.toggleSprintEnabled, () -> s.toggleSprintEnabled = !s.toggleSprintEnabled);
        registerToggle(2, 0, 2, "View Bobbing", () -> this.mc.gameSettings.viewBobbing && s.viewBobbingEnabled, () -> {
            boolean nextState = !(this.mc.gameSettings.viewBobbing && s.viewBobbingEnabled);
            this.mc.gameSettings.viewBobbing = nextState;
            s.viewBobbingEnabled = nextState;
            this.mc.gameSettings.saveOptions();
        });
        registerToggle(3, 0, 3, "FOV Effects", () -> s.fovEffectsEnabled, () -> s.fovEffectsEnabled = !s.fovEffectsEnabled);
        registerToggle(4, 0, 4, "Render Sky", () -> s.renderSky, () -> s.renderSky = !s.renderSky);

        // Right Column (1)
        registerToggle(5, 1, 0, "Disable All Fog", () -> s.disableAllFog, () -> s.disableAllFog = !s.disableAllFog);
        registerToggle(6, 1, 1, "Disable Terrain Fog", () -> s.disableTerrainFog, () -> s.disableTerrainFog = !s.disableTerrainFog);
        registerToggle(7, 1, 2, "Disable Void Fog", () -> s.disableVoidFog, () -> s.disableVoidFog = !s.disableVoidFog);
        registerToggle(8, 1, 3, "Disable Water Fog", () -> s.disableWaterFog, () -> s.disableWaterFog = !s.disableWaterFog);
        registerToggle(9, 1, 4, "Fullbright", () -> s.fullbrightEnabled, () -> s.fullbrightEnabled = !s.fullbrightEnabled);

        int leftX = this.width / 2 - 205;
        int rightX = this.width / 2 + 5;

        for (int i = 0; i < toggleEntries.size(); i++) {
            ToggleEntry entry = toggleEntries.get(i);
            int x = entry.column == 0 ? leftX : rightX;
            int y = START_Y + entry.row * ROW_SPACING;
            this.buttonList.add(new GuiButton(entry.id, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, entry.formatDisplayString()));
        }

        // Bottom navigation buttons
        int centerX = this.width / 2 - (BUTTON_WIDTH / 2);
        this.buttonList.add(new GuiButton(ID_KEYBINDS, centerX, this.height - 52, BUTTON_WIDTH, BUTTON_HEIGHT, "Keybinds"));
        this.buttonList.add(new GuiButton(ID_DONE, centerX, this.height - 28, BUTTON_WIDTH, BUTTON_HEIGHT, "Done"));
    }

    private void registerToggle(int id, int column, int row, String label, BooleanSupplier stateGetter, Runnable toggler) {
        toggleEntries.add(new ToggleEntry(id, column, row, label, stateGetter, toggler));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == ID_KEYBINDS) {
            this.mc.displayGuiScreen(new GuiModKeybinds(this));
            return;
        }
        if (button.id == ID_DONE) {
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }

        for (int i = 0; i < toggleEntries.size(); i++) {
            ToggleEntry entry = toggleEntries.get(i);
            if (entry.id == button.id) {
                entry.toggler.run();
                OriginalPvP.getInstance().getSettingsManager().save();
                button.displayString = entry.formatDisplayString();
                return;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Original PvP Settings", this.width / 2, 15, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static final class ToggleEntry {
        final int id;
        final int column;
        final int row;
        final String label;
        final BooleanSupplier stateGetter;
        final Runnable toggler;

        ToggleEntry(int id, int column, int row, String label, BooleanSupplier stateGetter, Runnable toggler) {
            this.id = id;
            this.column = column;
            this.row = row;
            this.label = label;
            this.stateGetter = stateGetter;
            this.toggler = toggler;
        }

        String formatDisplayString() {
            return label + ": " + (stateGetter.getAsBoolean() ? "\u00a7aON" : "\u00a7cOFF");
        }
    }
}
