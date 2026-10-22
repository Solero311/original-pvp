package de.originalpvp.hud;

import de.originalpvp.OriginalPvP;
import de.originalpvp.features.ToggleSprint;
import de.originalpvp.settings.ModSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Renders the in-game Original PvP HUD overlay (Toggle Sprint status indicator).
 */
public class HudRenderer {

    private static final float HUD_SCALE = 0.8f;
    private static final int HUD_MARGIN = 4;
    private static final int FONT_HEIGHT = 8;

    private static final int COLOR_ENABLED = 0x00FF00;
    private static final int COLOR_DISABLED = 0xFF5555;

    private static final String TEXT_SPRINT_ENABLED = "sprint enabled";
    private static final String TEXT_SPRINT_DISABLED = "sprint disabled";

    private final ModSettings settings;

    public HudRenderer(ModSettings settings) {
        this.settings = settings;
    }

    public void render(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.currentScreen != null) {
            return;
        }

        if (!settings.toggleSprintEnabled) {
            return;
        }

        OriginalPvP mod = OriginalPvP.getInstance();
        if (mod == null) {
            return;
        }

        ToggleSprint toggleSprint = mod.getToggleSprint();
        if (toggleSprint == null) {
            return;
        }

        boolean sprintToggled = toggleSprint.isToggled();
        String text = sprintToggled ? TEXT_SPRINT_ENABLED : TEXT_SPRINT_DISABLED;
        int color = sprintToggled ? COLOR_ENABLED : COLOR_DISABLED;

        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer fr = mc.fontRendererObj;

        GlStateManager.pushMatrix();
        GlStateManager.scale(HUD_SCALE, HUD_SCALE, HUD_SCALE);

        int textWidth = fr.getStringWidth(text);
        float x = (sr.getScaledWidth() - (textWidth * HUD_SCALE) - HUD_MARGIN) / HUD_SCALE;
        float y = (sr.getScaledHeight() - (FONT_HEIGHT * HUD_SCALE) - HUD_MARGIN) / HUD_SCALE;

        fr.drawStringWithShadow(text, x, y, color);

        GlStateManager.popMatrix();
    }
}
