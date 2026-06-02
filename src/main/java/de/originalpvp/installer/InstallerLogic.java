package de.originalpvp.installer;

import de.originalpvp.patcher.ClassPatcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Headless installation logic for the Original PvP mod.
 *
 * <ol>
 *   <li>Copies {@code .minecraft/versions/1.8.9/1.8.9.jar} to a new version directory</li>
 *   <li>Strips META-INF signatures</li>
 *   <li>Applies ASM patches to vanilla classes via {@link ClassPatcher}</li>
 *   <li>Injects mod classes from the running JAR</li>
 *   <li>Creates a version JSON and launcher profile</li>
 * </ol>
 */
public class InstallerLogic {

    private static final String VERSION_ID = "1.8.9-OriginalPvP";

    // ── callback ────────────────────────────────────────────────────────

    public interface ProgressCallback {
        void onProgress(int percent, String status);
    }

    // ── main install flow ───────────────────────────────────────────────

    public static void install(File minecraftDir, ProgressCallback cb) throws Exception {

        // 1. Validate that vanilla 1.8.9 exists
        File vanillaJar  = new File(minecraftDir, "versions/1.8.9/1.8.9.jar");
        File vanillaJson = new File(minecraftDir, "versions/1.8.9/1.8.9.json");

        if (!vanillaJar.exists()) {
            throw new FileNotFoundException(
                    "Minecraft 1.8.9 not found! Please run Minecraft 1.8.9 once first.\n" +
                    "Expected: " + vanillaJar.getAbsolutePath());
        }

        // 2. Create the new version directory
        cb.onProgress(5, "Creating version directory...");
        File versionDir = new File(minecraftDir, "versions/" + VERSION_ID);
        versionDir.mkdirs();

        // 3. Copy the vanilla JAR
        cb.onProgress(10, "Copying Minecraft 1.8.9 JAR...");
        File targetJar = new File(versionDir, VERSION_ID + ".jar");
        Files.copy(vanillaJar.toPath(), targetJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // 4. Read every entry from the copied JAR into memory
        cb.onProgress(20, "Reading JAR entries...");
        Map<String, byte[]> entries = new LinkedHashMap<String, byte[]>();
        try (JarFile jar = new JarFile(targetJar)) {
            Enumeration<JarEntry> en = jar.entries();
            while (en.hasMoreElements()) {
                JarEntry entry = en.nextElement();
                if (entry.isDirectory()) {
                    entries.put(entry.getName(), null);
                    continue;
                }
                entries.put(entry.getName(), readStream(jar.getInputStream(entry)));
            }
        }

        // 5. Remove META-INF (signatures + manifest)
        cb.onProgress(30, "Removing signatures...");
        Iterator<String> it = entries.keySet().iterator();
        while (it.hasNext()) {
            String name = it.next();
            if (name.startsWith("META-INF/")) {
                it.remove();
            }
        }

        // 6. Apply ASM patches to vanilla classes
        cb.onProgress(40, "Patching vanilla classes...");
        ClassPatcher.applyPatches(entries);

        // 7. Inject mod classes from the running JAR
        cb.onProgress(55, "Installing mod classes...");
        injectModClasses(entries);

        // 8. Write the final patched JAR
        cb.onProgress(70, "Writing patched JAR...");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(targetJar))) {
            for (Map.Entry<String, byte[]> e : entries.entrySet()) {
                ZipEntry ze = new ZipEntry(e.getKey());
                jos.putNextEntry(ze);
                if (e.getValue() != null) {
                    jos.write(e.getValue());
                }
                jos.closeEntry();
            }
        }

        // 9. Create version JSON
        cb.onProgress(80, "Creating version JSON...");
        createVersionJson(vanillaJson, versionDir);

        // 10. Register launcher profile
        cb.onProgress(90, "Updating launcher profiles...");
        updateLauncherProfiles(minecraftDir);

        cb.onProgress(100, "Installation complete!");
    }

    // ── inject mod classes from self-JAR ─────────────────────────────────

    private static void injectModClasses(Map<String, byte[]> entries) throws Exception {
        URI sourceUri = InstallerLogic.class
                .getProtectionDomain().getCodeSource().getLocation().toURI();
        File sourceFile = new File(sourceUri);

        if (!sourceFile.isFile() || !sourceFile.getName().endsWith(".jar")) {
            System.out.println("[OriginalPvP] Running from class directory – " +
                               "skipping mod-class injection (dev mode).");
            return;
        }

        try (JarFile ourJar = new JarFile(sourceFile)) {
            Enumeration<JarEntry> en = ourJar.entries();
            while (en.hasMoreElements()) {
                JarEntry entry = en.nextElement();
                if (entry.isDirectory()) continue;
                String name = entry.getName();

                // Skip META-INF and installer-only classes
                if (name.startsWith("META-INF/"))                   continue;
                if (name.startsWith("de/originalpvp/installer/"))   continue;

                // Include mod classes + relocated ASM
                if (name.startsWith("de/originalpvp/")
                        || name.startsWith("org/objectweb/asm/")
                        || name.startsWith("de/originalpvp/lib/asm/")) {
                    entries.put(name, readStream(ourJar.getInputStream(entry)));
                }
            }
        }
        System.out.println("[OriginalPvP] Injected mod classes.");
    }

    // ── version JSON ────────────────────────────────────────────────────

    private static void createVersionJson(File vanillaJson, File versionDir) throws IOException {
        String json;

        if (vanillaJson.exists()) {
            json = new String(Files.readAllBytes(vanillaJson.toPath()), "UTF-8");
            // Replace the id field with our version id
            json = json.replaceFirst(
                    "\"id\"\\s*:\\s*\"[^\"]*\"",
                    "\"id\": \"" + VERSION_ID + "\"");
        } else {
            // Minimal fallback using inheritsFrom (the launcher resolves the rest)
            json = "{\n"
                 + "  \"id\": \"" + VERSION_ID + "\",\n"
                 + "  \"inheritsFrom\": \"1.8.9\",\n"
                 + "  \"type\": \"release\",\n"
                 + "  \"mainClass\": \"net.minecraft.client.main.Main\",\n"
                 + "  \"minimumLauncherVersion\": 14\n"
                 + "}";
        }

        File target = new File(versionDir, VERSION_ID + ".json");
        Files.write(target.toPath(), json.getBytes("UTF-8"));
        System.out.println("[OriginalPvP] Created " + target.getName());
    }

    // ── launcher_profiles.json ──────────────────────────────────────────

    private static void updateLauncherProfiles(File minecraftDir) throws IOException {
        File profilesFile = new File(minecraftDir, "launcher_profiles.json");
        if (!profilesFile.exists()) {
            System.out.println("[OriginalPvP] launcher_profiles.json not found – skipping profile creation.");
            return;
        }

        String content = new String(Files.readAllBytes(profilesFile.toPath()), "UTF-8");

        // Don't add twice
        if (content.contains(VERSION_ID)) {
            System.out.println("[OriginalPvP] Launcher profile already exists.");
            return;
        }

        // Build our profile entry
        String profileId = UUID.randomUUID().toString().replace("-", "");
        String profileEntry =
                "\"" + profileId + "\": {\n"
              + "      \"name\": \"" + VERSION_ID + "\",\n"
              + "      \"lastVersionId\": \"" + VERSION_ID + "\",\n"
              + "      \"type\": \"custom\",\n"
              + "      \"icon\": \"Furnace\"\n"
              + "    }";

        // Insert right after the opening brace of "profiles": { ... }
        int profilesIdx = content.indexOf("\"profiles\"");
        if (profilesIdx == -1) {
            System.out.println("[OriginalPvP] Could not find \"profiles\" key – skipping.");
            return;
        }
        int braceIdx = content.indexOf('{', profilesIdx + 10);
        if (braceIdx == -1) {
            System.out.println("[OriginalPvP] Malformed launcher_profiles.json – skipping.");
            return;
        }

        // Check if the profiles object is empty
        String afterBrace = content.substring(braceIdx + 1).trim();
        String separator = afterBrace.startsWith("}") ? "\n    " : "\n    ";
        String ending    = afterBrace.startsWith("}") ? "\n"    : ",\n";

        content = content.substring(0, braceIdx + 1)
                + separator + profileEntry + ending
                + content.substring(braceIdx + 1);

        Files.write(profilesFile.toPath(), content.getBytes("UTF-8"));
        System.out.println("[OriginalPvP] Added launcher profile.");
    }

    // ── utility ─────────────────────────────────────────────────────────

    private static byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len;
        while ((len = is.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        is.close();
        return baos.toByteArray();
    }
}
