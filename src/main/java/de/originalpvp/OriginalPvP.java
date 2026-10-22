package de.originalpvp;

import de.originalpvp.core.ModHooks;
import de.originalpvp.features.FeatureManager;
import de.originalpvp.features.FogManager;
import de.originalpvp.features.FovEffects;
import de.originalpvp.features.FullbrightManager;
import de.originalpvp.features.SkyManager;
import de.originalpvp.features.ToggleSprint;
import de.originalpvp.features.ViewBobbing;
import de.originalpvp.features.Zoom;
import de.originalpvp.hud.HudRenderer;
import de.originalpvp.installer.GuiInstaller;
import de.originalpvp.patcher.ClassPatcher;
import de.originalpvp.settings.ModSettings;
import de.originalpvp.settings.SettingsManager;
import de.originalpvp.util.ModLogger;
import net.minecraft.client.settings.KeyBinding;

import java.util.List;

/**
 * Main entry point and service container for the Original PvP mod.
 *
 * <ul>
 *   <li><b>Installer mode:</b> When run via {@code java -jar}, launches the Swing installer GUI.</li>
 *   <li><b>Build-patch mode:</b> When invoked with {@code --build-patch}, extracts pre-patched vanilla classes.</li>
 *   <li><b>Runtime mode:</b> Holds the initialized mod instance, settings, and {@link FeatureManager}.</li>
 * </ul>
 */
public class OriginalPvP {

    private static OriginalPvP instance;

    public static KeyBinding keyBindGui;
    public static KeyBinding keyBindZoom;
    public static KeyBinding keyBindSprintMod;

    private final ModSettings settings;
    private final SettingsManager settingsManager;
    private final FeatureManager featureManager;

    private final Zoom zoom;
    private final ToggleSprint toggleSprint;
    private final FogManager fogManager;
    private final SkyManager skyManager;
    private final ViewBobbing viewBobbing;
    private final FovEffects fovEffects;
    private final FullbrightManager fullbrightManager;
    private final HudRenderer hudRenderer;

    private OriginalPvP() {
        this.settings = new ModSettings();
        this.settingsManager = new SettingsManager(this.settings);
        this.settingsManager.load();

        this.featureManager = new FeatureManager();
        this.zoom = this.featureManager.register(new Zoom(this.settings));
        this.toggleSprint = this.featureManager.register(new ToggleSprint(this.settings));
        this.fogManager = this.featureManager.register(new FogManager(this.settings));
        this.skyManager = this.featureManager.register(new SkyManager(this.settings));
        this.viewBobbing = this.featureManager.register(new ViewBobbing(this.settings));
        this.fovEffects = this.featureManager.register(new FovEffects(this.settings));
        this.fullbrightManager = this.featureManager.register(new FullbrightManager(this.settings));

        this.hudRenderer = new HudRenderer(this.settings);
    }

    // ── Standalone Entry Point ───────────────────────────────────────────

    public static void main(String[] args) {
        if (args.length >= 3 && "--build-patch".equals(args[0])) {
            try {
                ClassPatcher.extractPatchedClasses(args[1], args[2]);
                ModLogger.info("Build-time patching complete.");
            } catch (Exception e) {
                ModLogger.error("Build patching failed!", e);
                System.exit(1);
            }
            return;
        }

        GuiInstaller.launch();
    }

    // ── Singleton Lifecycle ──────────────────────────────────────────────

    public static OriginalPvP initializeInstance() {
        instance = new OriginalPvP();
        return instance;
    }

    public static OriginalPvP getInstance() {
        return instance;
    }

    // ── ASM Bytecode Hook Forwarding (delegates to ModHooks) ─────────────

    public static void init() {
        ModHooks.onGameInit();
    }

    public static void onTick() {
        ModHooks.onClientTick();
    }

    public static void onRenderOverlay(float partialTicks) {
        ModHooks.onRenderOverlay(partialTicks);
    }

    public static float hookGetFOVModifier(float originalFov, float partialTicks, boolean useFOVSetting) {
        return ModHooks.onGetFovModifier(originalFov, partialTicks, useFOVSetting);
    }

    public static void hookSetupFog() {
        ModHooks.onSetupFog();
    }

    public static boolean hookCancelItemScroll() {
        return ModHooks.shouldCancelItemScroll();
    }

    public static void hookF3Menu(List<String> list) {
        ModHooks.onDebugOverlayText(list);
    }

    public static boolean hookShouldRenderSky() {
        return ModHooks.shouldRenderSky();
    }

    public static boolean hookShouldApplyBobbing() {
        return ModHooks.shouldApplyBobbing();
    }

    // ── Service & Feature Getters ────────────────────────────────────────

    public ModSettings getSettings() {
        return settings;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
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

    public FullbrightManager getFullbrightManager() {
        return fullbrightManager;
    }

    public HudRenderer getHudRenderer() {
        return hudRenderer;
    }
}
