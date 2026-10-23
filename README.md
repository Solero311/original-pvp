# Original PvP

**Original PvP** is a lightweight, standalone Minecraft **1.8.9** client-side JAR mod that enhances the classic vanilla PvP experience with essential quality-of-life and visual features—without requiring Forge or Fabric at runtime.

---

## Features
- **Toggle Sprint:** Keep sprinting without holding the sprint key, smoothly integrated with native vanilla movement and sneak mechanics.
- **Smooth Zoom:** Interpolated zoom with real-time mouse-wheel FOV adjustment (`10° – 110°`) and automatic hotbar scroll locking while active.
- **Fog Control:** Granular toggles to disable **All Fog**, **Terrain Fog**, **Water/Lava Fog**, or **Void Fog** while keeping the sky intact.
- **Fullbright:** Instant maximum brightness toggle that automatically restores your previous custom gamma setting when turned off.
- **Sky Rendering Toggle:** Enable or disable sky rendering on the fly.
- **View Bobbing Control:** Quickly toggle camera bobbing directly from the mod menu.
- **FOV Effects Toggle:** Prevent FOV distortion (stretching) while sprinting, flying, or under speed/slowness potion effects.
- **In-Game Mod Menu & Keybinds:** Accessible anytime via hotkey (`Right Shift` by default) or via **Options → Original PvP Settings**, and fully synced with Vanilla Controls.
- **HUD Status Indicator:** Clean, unobtrusive Toggle Sprint status display in the bottom-right corner.

---

## Default Keybinds

| Action | Default Key | Configurable In |
| :--- | :--- | :--- |
| **Open Mod Menu** | `Right Shift` | Mod Menu → Keybinds / Vanilla Controls |
| **Zoom (Hold)** | `V` | Mod Menu → Keybinds / Vanilla Controls |
| **Toggle Sprint** | `R` | Mod Menu → Keybinds / Vanilla Controls |

---

## Installation

Download **`OriginalPvP-2.0.jar`** from the [Releases](https://github.com/Solero311/original-pvp/releases) page.

### Option 1: Prism Launcher / MultiMC (Recommended)
Because Original PvP is a **Vanilla JAR Mod** (not a Forge/Fabric mod), add it directly to `Minecraft.jar` instead of the `mods` folder:
1. Right-click your **1.8.9** instance and select **Edit**.
2. Go to the **Version** tab.
3. Click **Add to Minecraft.jar** on the right-hand side.
4. Select `OriginalPvP-2.0.jar` and launch the game.

### Option 2: Official Minecraft Launcher (Standalone Installer)
1. Ensure you have launched vanilla **Minecraft 1.8.9** at least once.
2. Double-click `OriginalPvP-2.0.jar` or run:
   ```bash
   java -jar OriginalPvP-2.0.jar
   ```
3. Verify your `.minecraft` directory and click **Install**.
4. Open the Minecraft Launcher, select the **`1.8.9-OriginalPvP`** profile, and click **Play**.

---

## Architecture (v2.0 Rewrite)

```text
src/main/java/de/originalpvp/
├── OriginalPvP.java            # Core singleton & CLI entry point
├── core/
│   └── ModHooks.java           # Runtime bridge for ASM-injected vanilla callbacks
├── features/                   # Modular IFeature implementations & FeatureManager
├── gui/                        # In-game settings (GuiModMenu) & keybind screens
├── hud/                        # HUD overlay renderer
├── installer/                  # Standalone Swing installer & profile generator
├── patcher/                    # Modular ASM bytecode transformers & Notch mappings
├── settings/                   # Atomic configuration persistence (config/originalpvp.properties)
└── util/                       # Shared JAR I/O, constants & logging utilities
```

---

## Building from Source

**Requirements:** **Java 8 (JDK 1.8)** is required for the ForgeGradle 2.1 toolchain.

```bash
# Linux / macOS
./gradlew clean build

# Windows
gradlew.bat clean build
```

The final, ready-to-use JAR is generated at:
`build/libs/OriginalPvP-2.0.jar`
