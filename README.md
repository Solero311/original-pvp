# Original PvP

Original PvP is a 1.8.9 client-side mod that restores and enhances the vanilla PvP experience with a suite of aesthetic and functional features.

## Features
- **Toggle Sprint**: Keep sprinting without holding the key, smoothly integrated with vanilla Minecraft movement.
- **Zoom**: Smooth Optifine-style zoom, bound to a key with adjustable sensitivity.
- **Fog Control**: Disable water fog and/or terrain fog for clearer vision while keeping the sky rendered perfectly.
- **Sky & Fullbright Settings**: Easily toggle sky rendering and maximum brightness.
- **View Bobbing Tweaks**: Adjust or completely disable view bobbing.
- **FOV Effects**: Prevent FOV distortion (stretching) while sprinting or under potion effects.
- **In-Game Mod Menu**: Easy-to-use custom GUI (defaults to Right Shift) to toggle all settings.
- **HUD Information**: Displays Toggle Sprint status cleanly on your HUD.
- **1.8.9 Support**: Built on Forge 1.8.9 / MCP stable_22 mappings.

## Installation
Original PvP supports installation via its standalone installer.
Simply run: `java -jar OriginalPvP-1.1.4.jar` to open the installer GUI, or add it to your instances manually.

## Building from Source
**Requirements:** You must use **Java 8 (JDK 1.8)** to build this project, as newer Java versions are not compatible with the old ForgeGradle 2.1 toolchain.

Run `./gradlew clean build` (Mac/Linux) or `gradlew.bat clean build` (Windows) in the repository root. 
The compiled JAR will be generated in `build/libs/`.
