package de.originalpvp.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import de.originalpvp.OriginalPvP;
import de.originalpvp.settings.ModSettings;

public class HudRenderer {

    private final ModSettings settings;

    public HudRenderer(ModSettings settings) {
        this.settings = settings;
    }

    public void render(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.thePlayer == null || mc.currentScreen != null) {
            return;
        }

        // Only show sprint indicator when toggle sprint is enabled and player is sprinting
        if (!this.settings.toggleSprintEnabled) {
            return;
        }

        OriginalPvP mod = OriginalPvP.getInstance();
        if (mod == null || mod.getToggleSprint() == null) {
            return;
        }
        // Always show the status if the feature is enabled
        String text = mod.getToggleSprint().isToggled() ? "sprint enabled" : "sprint disabled";
        int color = mod.getToggleSprint().isToggled() ? 0x00FF00 : 0xFF5555;
        
        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer fr = mc.fontRendererObj;

        // Scale down to 0.8x size
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.8f, 0.8f, 0.8f);

        int textWidth = fr.getStringWidth(text);
        
        // Calculate position based on the 0.8 scale (inverse scale the coords)
        int x = (int) ((sr.getScaledWidth() - (textWidth * 0.8f) - 4) / 0.8f);
        int y = (int) ((sr.getScaledHeight() - (8 * 0.8f) - 4) / 0.8f);

        // Draw text with shadow, no background
        fr.drawStringWithShadow(text, (float) x, (float) y, color);

        GlStateManager.popMatrix();
    }
}
