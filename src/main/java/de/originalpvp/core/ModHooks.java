package de.originalpvp.core;

import de.originalpvp.OriginalPvP;
import de.originalpvp.gui.GuiModMenu;
import de.originalpvp.settings.ModSettings;
import de.originalpvp.util.ModConstants;
import de.originalpvp.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.List;

/**
 * Runtime hook dispatcher invoked by ASM-instrumented vanilla Minecraft classes.
 * Separates bytecode callback handling and error boundaries from core feature logic.
 */
public final class ModHooks {

    private static boolean menuKeyWasDown = false;

    private ModHooks() {
        // Prevent instantiation
    }

    /**
     * Hooked at the end of {@code Minecraft.startGame()}.
     */
    public static void onGameInit() {
        try {
            OriginalPvP mod = OriginalPvP.initializeInstance();
            Minecraft mc = Minecraft.getMinecraft();

            registerVanillaKeybindings(mc, mod.getSettings());
            mod.getFeatureManager().initAll(mc);

            ModLogger.info("Mod initialized successfully (" + ModConstants.FULL_DISPLAY_NAME + ").");
        } catch (Exception e) {
            ModLogger.error("Initialization failed!", e);
        }
    }

    /**
     * Hooked at the start of {@code Minecraft.runTick()}.
     */
    public static void onClientTick() {
        try {
            OriginalPvP mod = OriginalPvP.getInstance();
            if (mod == null) {
                return;
            }

            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null) {
                return;
            }

            syncKeybindingsFromVanilla(mod);
            handleMenuKeybind(mc, mod.getSettings());
            mod.getFeatureManager().tickAll(mc);
        } catch (Exception e) {
            ModLogger.error("Error in onClientTick", e);
        }
    }

    /**
     * Hooked after {@code GuiIngame.renderGameOverlay(float)} in {@code EntityRenderer.updateCameraAndRender}.
     */
    public static void onRenderOverlay(float partialTicks) {
        try {
            OriginalPvP mod = OriginalPvP.getInstance();
            if (mod == null) {
                return;
            }
            mod.getHudRenderer().render(partialTicks);
        } catch (Exception e) {
            ModLogger.error("Error in onRenderOverlay", e);
        }
    }

    /**
     * Hooked before return in {@code EntityRenderer.getFOVModifier(float, boolean)}.
     */
    public static float onGetFovModifier(float originalFov, float partialTicks, boolean useFovSetting) {
        try {
            OriginalPvP mod = OriginalPvP.getInstance();
            if (mod == null || !useFovSetting) {
                // Do not alter hand rendering FOV (!useFovSetting)
                return originalFov;
            }

            Minecraft mc = Minecraft.getMinecraft();
            float baseFov = mod.getFovEffects().resolveFov(mc, originalFov);

            if (mod.getZoom().shouldModifyFov()) {
                return mod.getZoom().getZoomFov(baseFov, partialTicks);
            }

            return baseFov;
        } catch (Exception e) {
            ModLogger.error("Error in onGetFovModifier", e);
            return originalFov;
        }
    }

    /**
     * Hooked at the end of {@code EntityRenderer.setupFog(int, float)}.
     */
    public static void onSetupFog() {
        try {
            OriginalPvP mod = OriginalPvP.getInstance();
            if (mod == null) {
                return;
            }
            mod.getFogManager().applyFogOverrides(Minecraft.getMinecraft());
        } catch (Exception e) {
            ModLogger.error("Error in onSetupFog", e);
        }
    }

    /**
     * Hooked at the start of {@code InventoryPlayer.changeCurrentItem(int)}.
     *
     * @return {@code true} if hotbar scrolling should be cancelled
     */
    public static boolean shouldCancelItemScroll() {
        try {
            OriginalPvP mod = OriginalPvP.getInstance();
            return mod != null && mod.getZoom().isZooming();
        } catch (Exception e) {
            ModLogger.error("Error in shouldCancelItemScroll", e);
            return false;
        }
    }

    /**
     * Hooked before return in {@code GuiOverlayDebug.call()}.
     */
    public static void onDebugOverlayText(List<String> lines) {
        try {
            if (lines != null && lines.size() > 1) {
                lines.add(1, ModConstants.FULL_DISPLAY_NAME);
            }
        } catch (Exception e) {
            ModLogger.error("Error in onDebugOverlayText", e);
        }
    }

    /**
     * Hooked at the start of {@code RenderGlobal.renderSky(float, int)}.
     *
     * @return {@code true} if sky rendering should proceed
     */
    public static boolean shouldRenderSky() {
        try {
            OriginalPvP mod = OriginalPvP.getInstance();
            return mod == null || mod.getSkyManager().shouldRenderSky();
        } catch (Exception e) {
            ModLogger.error("Error in shouldRenderSky", e);
            return true;
        }
    }

    /**
     * Hooked in {@code EntityRenderer.orientCamera(float)} after reading {@code GameSettings.viewBobbing}.
     *
     * @return {@code true} if camera bobbing is allowed by the mod
     */
    public static boolean shouldApplyBobbing() {
        try {
            OriginalPvP mod = OriginalPvP.getInstance();
            return mod == null || mod.getViewBobbing().shouldApplyBobbing();
        } catch (Exception e) {
            ModLogger.error("Error in shouldApplyBobbing", e);
            return true;
        }
    }

    // ── Internal Helpers ─────────────────────────────────────────────────

    private static void registerVanillaKeybindings(Minecraft mc, ModSettings settings) {
        OriginalPvP.keyBindGui = new KeyBinding("Original PvP Menu", settings.menuKeyCode, ModConstants.KEYBIND_CATEGORY);
        OriginalPvP.keyBindZoom = new KeyBinding("Zoom", settings.zoomKeyCode, ModConstants.KEYBIND_CATEGORY);
        OriginalPvP.keyBindSprintMod = new KeyBinding("Toggle Sprint", settings.toggleSprintKeyCode, ModConstants.KEYBIND_CATEGORY);

        if (mc != null && mc.gameSettings != null && mc.gameSettings.keyBindings != null) {
            KeyBinding[] current = mc.gameSettings.keyBindings;
            KeyBinding[] extended = new KeyBinding[current.length + 3];
            System.arraycopy(current, 0, extended, 0, current.length);
            extended[current.length] = OriginalPvP.keyBindGui;
            extended[current.length + 1] = OriginalPvP.keyBindZoom;
            extended[current.length + 2] = OriginalPvP.keyBindSprintMod;
            mc.gameSettings.keyBindings = extended;
        }
    }

    private static void syncKeybindingsFromVanilla(OriginalPvP mod) {
        if (OriginalPvP.keyBindGui == null || OriginalPvP.keyBindZoom == null || OriginalPvP.keyBindSprintMod == null) {
            return;
        }

        ModSettings settings = mod.getSettings();
        int guiCode = OriginalPvP.keyBindGui.getKeyCode();
        int zoomCode = OriginalPvP.keyBindZoom.getKeyCode();
        int sprintCode = OriginalPvP.keyBindSprintMod.getKeyCode();

        boolean changed = false;
        if (settings.menuKeyCode != guiCode) {
            settings.menuKeyCode = guiCode;
            changed = true;
        }
        if (settings.zoomKeyCode != zoomCode) {
            settings.zoomKeyCode = zoomCode;
            changed = true;
        }
        if (settings.toggleSprintKeyCode != sprintCode) {
            settings.toggleSprintKeyCode = sprintCode;
            changed = true;
        }

        if (changed) {
            mod.getSettingsManager().save();
        }
    }

    private static void handleMenuKeybind(Minecraft mc, ModSettings settings) {
        boolean menuKeyDown = mc.currentScreen == null
                && settings.menuKeyCode > 0
                && Keyboard.isKeyDown(settings.menuKeyCode);

        if (menuKeyDown && !menuKeyWasDown) {
            mc.displayGuiScreen(new GuiModMenu(null));
        }
        menuKeyWasDown = menuKeyDown;
    }
}
