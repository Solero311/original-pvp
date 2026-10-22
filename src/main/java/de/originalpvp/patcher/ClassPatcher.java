package de.originalpvp.patcher;

import de.originalpvp.patcher.transformers.EntityRendererTransformer;
import de.originalpvp.patcher.transformers.GuiOptionsTransformer;
import de.originalpvp.patcher.transformers.GuiOverlayDebugTransformer;
import de.originalpvp.patcher.transformers.InventoryPlayerTransformer;
import de.originalpvp.patcher.transformers.MinecraftTransformer;
import de.originalpvp.patcher.transformers.RenderGlobalTransformer;
import de.originalpvp.util.JarUtils;
import de.originalpvp.util.ModLogger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Coordinates ASM bytecode transformers to patch vanilla Minecraft 1.8.9 classes
 * during standalone installation or Gradle build-time extraction.
 */
public class ClassPatcher {

    private static final List<IClassTransformer> TRANSFORMERS = Collections.unmodifiableList(Arrays.asList(
            new MinecraftTransformer(),
            new EntityRendererTransformer(),
            new RenderGlobalTransformer(),
            new GuiOptionsTransformer(),
            new InventoryPlayerTransformer(),
            new GuiOverlayDebugTransformer()
    ));

    /**
     * Patches an existing Minecraft 1.8.9 JAR file in-place (creating a {@code .backup} copy first).
     */
    public static void patchMinecraftJar(String jarPath) throws Exception {
        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            ModLogger.error("File not found: " + jarPath);
            return;
        }

        ModLogger.info("Patching Minecraft JAR: " + jarPath);

        File backupFile = new File(jarPath + ".backup");
        if (!backupFile.exists()) {
            ModLogger.info("Creating backup: " + backupFile.getName());
            Files.copy(jarFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            ModLogger.info("Backup already exists, skipping.");
        }

        Map<String, byte[]> entries = JarUtils.readJarEntries(jarFile, false);
        applyPatches(entries);
        JarUtils.injectModClasses(ClassPatcher.class, entries, false);

        ModLogger.info("Writing patched JAR...");
        JarUtils.writeJarEntries(jarFile, entries);
        ModLogger.info("Patching complete! You can now launch Minecraft 1.8.9.");
    }

    /**
     * Applies all registered {@link IClassTransformer}s to the vanilla class entries in the map.
     */
    public static void applyPatches(Map<String, byte[]> entries) {
        for (int i = 0; i < TRANSFORMERS.size(); i++) {
            IClassTransformer transformer = TRANSFORMERS.get(i);
            String entryName = transformer.getEntryName();
            byte[] originalBytes = entries.get(entryName);

            if (originalBytes == null) {
                ModLogger.warn("Could not find " + entryName + " (" + transformer.getFriendlyName() + ") in JAR");
                continue;
            }

            ModLogger.info("Patching " + transformer.getFriendlyName() + " (" + entryName + ")...");
            byte[] patchedBytes = transformer.transform(originalBytes);
            entries.put(entryName, patchedBytes);
        }
    }

    /**
     * Build-time helper: reads target vanilla classes from a Minecraft 1.8.9 JAR,
     * applies ASM patches, and writes the transformed {@code .class} files to {@code outputDir}
     * for inclusion in the final JAR (enabling PrismLauncher / MultiMC JAR mod support).
     */
    public static void extractPatchedClasses(String vanillaJarPath, String outputDir) throws Exception {
        ModLogger.info("Extracting patched vanilla classes...");
        File vanillaFile = new File(vanillaJarPath);
        if (!vanillaFile.exists()) {
            ModLogger.warn("Vanilla JAR not found at " + vanillaJarPath + " - skipping build-time patching.");
            return;
        }

        Map<String, byte[]> entries = new LinkedHashMap<String, byte[]>();
        try (JarFile jar = new JarFile(vanillaFile)) {
            for (int i = 0; i < TRANSFORMERS.size(); i++) {
                IClassTransformer transformer = TRANSFORMERS.get(i);
                String entryName = transformer.getEntryName();
                JarEntry entry = jar.getJarEntry(entryName);
                if (entry != null) {
                    entries.put(entryName, JarUtils.readStream(jar.getInputStream(entry)));
                } else {
                    ModLogger.warn(entryName + " not found in vanilla JAR.");
                }
            }
        }

        applyPatches(entries);

        File outDir = new File(outputDir);
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
            File outFile = new File(outDir, entry.getKey());
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            Files.write(outFile.toPath(), entry.getValue());
            ModLogger.info("  -> " + entry.getKey());
        }
    }

    public static List<IClassTransformer> getTransformers() {
        return TRANSFORMERS;
    }
}
