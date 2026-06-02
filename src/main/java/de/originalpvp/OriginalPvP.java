package de.originalpvp;

import de.originalpvp.features.FogManager;
import de.originalpvp.features.FovEffects;
import de.originalpvp.features.SkyManager;
import de.originalpvp.features.ToggleSprint;
import de.originalpvp.features.ViewBobbing;
import de.originalpvp.features.Zoom;
import de.originalpvp.hud.HudRenderer;
import de.originalpvp.patcher.ClassPatcher;
import de.originalpvp.settings.ModSettings;
import de.originalpvp.settings.SettingsManager;
import net.minecraft.client.Minecraft;

import de.originalpvp.installer.GuiInstaller;

/**
 * Main entry point for the OriginalPvP mod.
 *
 * <p><b>Installer mode:</b> When run via {@code java -jar}, opens a Swing GUI
 * installer that patches the vanilla Minecraft 1.8.9 JAR and creates a
 * launcher profile.</p>
 *
 * <p><b>Build-patch mode:</b> When invoked with {@code --build-patch}, extracts
 * pre-patched vanilla classes for inclusion in the fat JAR (PrismLauncher
 * compatibility).</p>
 *
 * <p><b>Runtime mode:</b> Static hook methods are called from ASM-patched vanilla
 * code to drive every mod feature.</p>
 */
public class OriginalPvP {

    // ── singleton ────────────────────────────────────────────────────────
    private static OriginalPvP instance;

    // ── fields ───────────────────────────────────────────────────────────
    private ModSettings settings;
    private SettingsManager settingsManager;
    private Zoom zoom;
    private ToggleSprint toggleSprint;
    private FogManager fogManager;
    private SkyManager skyManager;
    private ViewBobbing viewBobbing;
    private FovEffects fovEffects;
    private HudRenderer hudRenderer;

    // ── entry point ─────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Build-patch mode: used by Gradle to extract pre-patched vanilla classes
        if (args.length >= 3 && "--build-patch".equals(args[0])) {
            try {
                ClassPatcher.extractPatchedClasses(args[1], args[2]);
                System.out.println("[OriginalPvP] Build-time patching complete.");
            } catch (Exception e) {
                System.err.println("[OriginalPvP] Build patching failed!");
                e.printStackTrace();
                System.exit(1);
            }
            return;
        }

        // Installer mode: open GUI
        GuiInstaller.launch();
    }

    // ── runtime hooks (called from ASM-injected code) ────────────────────

    public static net.minecraft.client.settings.KeyBinding keyBindGui;
    public static net.minecraft.client.settings.KeyBinding keyBindZoom;
    public static net.minecraft.client.settings.KeyBinding keyBindSprintMod;

    /**
     * Called once at the end of {@code Minecraft.startGame()}.
     * Creates the singleton, loads settings and instantiates every feature.
     */
    public static void init() {
        try {
            instance = new OriginalPvP();

            instance.settings = new ModSettings();
            instance.settingsManager = new SettingsManager(instance.settings);
            instance.settingsManager.load();

            instance.zoom = new Zoom(instance.settings);
            instance.toggleSprint = new ToggleSprint(instance.settings);
            instance.fogManager = new FogManager(instance.settings);
            instance.skyManager = new SkyManager(instance.settings);
            instance.viewBobbing = new ViewBobbing(instance.settings);
            instance.fovEffects = new FovEffects(instance.settings);
            instance.hudRenderer = new HudRenderer(instance.settings);

            // Register GUI Keybinds
            keyBindGui = new net.minecraft.client.settings.KeyBinding("Original PvP Menu", instance.settings.menuKeyCode, "Original PvP");
            keyBindZoom = new net.minecraft.client.settings.KeyBinding("Zoom", instance.settings.zoomKeyCode, "Original PvP");
            keyBindSprintMod = new net.minecraft.client.settings.KeyBinding("Toggle Sprint", instance.settings.toggleSprintKeyCode, "Original PvP");
            
            Minecraft mc = Minecraft.getMinecraft();
            net.minecraft.client.settings.KeyBinding[] current = mc.gameSettings.keyBindings;
            net.minecraft.client.settings.KeyBinding[] newBinds = new net.minecraft.client.settings.KeyBinding[current.length + 3];
            System.arraycopy(current, 0, newBinds, 0, current.length);
            newBinds[current.length] = keyBindGui;
            newBinds[current.length + 1] = keyBindZoom;
            newBinds[current.length + 2] = keyBindSprintMod;
            mc.gameSettings.keyBindings = newBinds;

            System.out.println("[OriginalPvP] Mod initialized successfully!");
        } catch (Exception e) {
            System.err.println("[OriginalPvP] Initialization failed!");
            e.printStackTrace();
        }
    }

    private static boolean menuKeyWasDown = false;

    /**
     * Called at the start of every {@code Minecraft.runTick()}.
     */
    public static void onTick() {
        try {
            if (instance == null) return;
            Minecraft mc = Minecraft.getMinecraft();

            // Sync keybinds from Vanilla Controls to our config in real-time
            if (keyBindGui != null && keyBindZoom != null && keyBindSprintMod != null) {
                instance.settings.menuKeyCode = keyBindGui.getKeyCode();
                instance.settings.zoomKeyCode = keyBindZoom.getKeyCode();
                instance.settings.toggleSprintKeyCode = keyBindSprintMod.getKeyCode();
            }

            // GUI Keybind Check (only if no menu is open to prevent chat typing trigger)
            boolean menuKeyDown = mc.currentScreen == null && org.lwjgl.input.Keyboard.isKeyDown(instance.settings.menuKeyCode);
            if (menuKeyDown && !menuKeyWasDown) {
                mc.displayGuiScreen(new de.originalpvp.gui.GuiModMenu(mc.currentScreen));
            }
            menuKeyWasDown = menuKeyDown;

            // Fullbright logic
            if (instance.settings.fullbrightEnabled) {
                if (mc.gameSettings.gammaSetting < 10.0f) {
                    mc.gameSettings.gammaSetting = 100.0f;
                }
            } else if (mc.gameSettings.gammaSetting > 1.0f) {
                mc.gameSettings.gammaSetting = 1.0f; // Reset if it was maxed
            }

            instance.zoom.onTick();
            instance.toggleSprint.onTick(mc);
        } catch (Exception e) {
            System.err.println("[OriginalPvP] Error in onTick");
            e.printStackTrace();
        }
    }

    /**
     * Called after the in-game overlay is rendered by {@code EntityRenderer.updateCameraAndRender}.
     */
    public static void onRenderOverlay(float partialTicks) {
        try {
            if (instance == null) return;
            instance.hudRenderer.render(partialTicks);
        } catch (Exception e) {
            System.err.println("[OriginalPvP] Error in onRenderOverlay");
            e.printStackTrace();
        }
    }

    /**
     * Replaces the FOV value returned by {@code EntityRenderer.getFOVModifier}.
     */
    public static float hookGetFOVModifier(float originalFov, float partialTicks, boolean useFOVSetting) {
        try {
            if (instance == null) return originalFov;

            // Differentiate between camera and hand rendering
            if (!useFOVSetting) {
                return originalFov; // It's hand rendering! Don't stretch the hand!
            }

            // Zoom takes absolute priority
            if (instance.zoom.isZooming()) {
                return instance.zoom.getZoomFov(originalFov, partialTicks);
            }

            // If FOV effects are disabled, return the raw setting value
            if (!instance.fovEffects.shouldApplyFovEffects()) {
                return Minecraft.getMinecraft().gameSettings.fovSetting;
            }

            return originalFov;
        } catch (Exception e) {
            System.err.println("[OriginalPvP] Error in hookGetFOVModifier");
            e.printStackTrace();
            return originalFov;
        }
    }

    /**
     * Called at the end of EntityRenderer.setupFog to override fog distances if disabled.
     */
    public static void hookSetupFog() {
        try {
            if (instance == null) return;
            boolean cancel = false;
            
            if (instance.settings.disableAllFog) {
                cancel = true;
            } else {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.thePlayer != null) {
                    boolean inWater = mc.thePlayer.isInsideOfMaterial(net.minecraft.block.material.Material.water) 
                                   || mc.thePlayer.isInsideOfMaterial(net.minecraft.block.material.Material.lava);
                    if (inWater && instance.settings.disableWaterFog) cancel = true;
                    if (!inWater && instance.settings.disableTerrainFog) cancel = true;
                }
            }
            
            if (cancel) {
                net.minecraft.client.renderer.GlStateManager.setFogStart(99999.0f);
                net.minecraft.client.renderer.GlStateManager.setFogEnd(199999.0f);
                net.minecraft.client.renderer.GlStateManager.setFogDensity(0.0f);
            }
        } catch (Exception e) {
            System.err.println("[OriginalPvP] Error in hookSetupFog");
            e.printStackTrace();
        }
    }

    /**
     * @return {@code true} if we should cancel mouse wheel item scrolling.
     */
    public static boolean hookCancelItemScroll() {
        try {
            if (instance == null) return false;
            return instance.zoom.isZooming();
        } catch (Exception e) {
            System.err.println("[OriginalPvP] Error in hookCancelItemScroll");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Called by GuiOverlayDebug.call() to inject our mod name into the F3 menu.
     */
    public static void hookF3Menu(java.util.List<String> list) {
        try {
            if (list != null && list.size() > 1) {
                list.add(1, "Original PvP 1.1.4");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return {@code true} if the sky should be rendered.
     */
    public static boolean hookShouldRenderSky() {
        try {
            if (instance == null) return true;
            return instance.skyManager.shouldRenderSky();
        } catch (Exception e) {
            System.err.println("[OriginalPvP] Error in hookShouldRenderSky");
            e.printStackTrace();
            return true;
        }
    }

    /**
     * @return {@code true} if view bobbing should be applied (AND-ed with vanilla setting).
     */
    public static boolean hookShouldApplyBobbing() {
        try {
            if (instance == null) return true;
            return instance.viewBobbing.shouldApplyBobbing();
        } catch (Exception e) {
            System.err.println("[OriginalPvP] Error in hookShouldApplyBobbing");
            e.printStackTrace();
            return true;
        }
    }

    // ── getters ──────────────────────────────────────────────────────────

    public static OriginalPvP getInstance() {
        return instance;
    }

    public ModSettings getSettings() {
        return settings;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public Zoom getZoom() {
        return zoom;
    }

    public ToggleSprint getToggleSprint() {
        return toggleSprint;
    }

    public FogManager getFogManager() {
        return fogManager;
    }

    public SkyManager getSkyManager() {
        return skyManager;
    }

    public ViewBobbing getViewBobbing() {
        return viewBobbing;
    }

    public FovEffects getFovEffects() {
        return fovEffects;
    }

    public HudRenderer getHudRenderer() {
        return hudRenderer;
    }
}
