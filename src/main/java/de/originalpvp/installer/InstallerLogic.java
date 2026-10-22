package de.originalpvp.installer;

import de.originalpvp.patcher.ClassPatcher;
import de.originalpvp.util.JarUtils;
import de.originalpvp.util.ModConstants;
import de.originalpvp.util.ModLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * Headless installation pipeline for the Original PvP standalone installer.
 *
 * <ol>
 *   <li>Copies {@code .minecraft/versions/1.8.9/1.8.9.jar} to a dedicated version directory</li>
 *   <li>Strips {@code META-INF} signatures and manifest</li>
 *   <li>Applies ASM bytecode patches via {@link ClassPatcher}</li>
 *   <li>Injects mod runtime classes via {@link JarUtils}</li>
 *   <li>Generates the version JSON and registers a launcher profile</li>
 * </ol>
 */
public class InstallerLogic {

    private static final String VERSION_ID = ModConstants.INSTALLER_VERSION_ID;

    public interface ProgressCallback {
        void onProgress(int percent, String status);
    }

    public static void install(File minecraftDir, ProgressCallback callback) throws Exception {
        ProgressCallback cb = callback != null ? callback : (p, s) -> {};

        File vanillaJar = new File(minecraftDir, "versions/1.8.9/1.8.9.jar");
        File vanillaJson = new File(minecraftDir, "versions/1.8.9/1.8.9.json");

        if (!vanillaJar.exists()) {
            throw new FileNotFoundException(
                    "Minecraft 1.8.9 not found! Please run Minecraft 1.8.9 once first.\n" +
                    "Expected: " + vanillaJar.getAbsolutePath());
        }

        cb.onProgress(5, "Creating version directory...");
        File versionDir = new File(minecraftDir, "versions/" + VERSION_ID);
        versionDir.mkdirs();

        cb.onProgress(10, "Copying Minecraft 1.8.9 JAR...");
        File targetJar = new File(versionDir, VERSION_ID + ".jar");
        Files.copy(vanillaJar.toPath(), targetJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

        cb.onProgress(25, "Reading JAR entries & removing signatures...");
        Map<String, byte[]> entries = JarUtils.readJarEntries(targetJar, true);

        cb.onProgress(45, "Patching vanilla classes...");
        ClassPatcher.applyPatches(entries);

        cb.onProgress(60, "Installing mod classes...");
        JarUtils.injectModClasses(InstallerLogic.class, entries, true);

        cb.onProgress(75, "Writing patched JAR...");
        JarUtils.writeJarEntries(targetJar, entries);

        cb.onProgress(85, "Creating version JSON...");
        createVersionJson(vanillaJson, versionDir);

        cb.onProgress(95, "Updating launcher profiles...");
        updateLauncherProfiles(minecraftDir);

        cb.onProgress(100, "Installation complete!");
    }

    private static void createVersionJson(File vanillaJson, File versionDir) throws IOException {
        String json;

        if (vanillaJson.exists()) {
            json = new String(Files.readAllBytes(vanillaJson.toPath()), StandardCharsets.UTF_8);
            json = json.replaceFirst(
                    "\"id\"\\s*:\\s*\"[^\"]*\"",
                    "\"id\": \"" + VERSION_ID + "\"");
        } else {
            json = "{\n"
                    + "  \"id\": \"" + VERSION_ID + "\",\n"
                    + "  \"inheritsFrom\": \"1.8.9\",\n"
                    + "  \"type\": \"release\",\n"
                    + "  \"mainClass\": \"net.minecraft.client.main.Main\",\n"
                    + "  \"minimumLauncherVersion\": 14\n"
                    + "}";
        }

        File target = new File(versionDir, VERSION_ID + ".json");
        Files.write(target.toPath(), json.getBytes(StandardCharsets.UTF_8));
        ModLogger.info("Created " + target.getName());
    }

    private static void updateLauncherProfiles(File minecraftDir) throws IOException {
        File profilesFile = new File(minecraftDir, "launcher_profiles.json");
        if (!profilesFile.exists()) {
            ModLogger.info("launcher_profiles.json not found - skipping profile creation.");
            return;
        }

        String content = new String(Files.readAllBytes(profilesFile.toPath()), StandardCharsets.UTF_8);

        if (content.contains(VERSION_ID)) {
            ModLogger.info("Launcher profile already exists.");
            return;
        }

        int profilesIdx = content.indexOf("\"profiles\"");
        if (profilesIdx == -1) {
            ModLogger.warn("Could not find \"profiles\" key in launcher_profiles.json - skipping.");
            return;
        }
        int braceIdx = content.indexOf('{', profilesIdx + 10);
        if (braceIdx == -1) {
            ModLogger.warn("Malformed launcher_profiles.json - skipping.");
            return;
        }

        String profileId = UUID.randomUUID().toString().replace("-", "");
        String profileEntry =
                "\"" + profileId + "\": {\n"
                        + "      \"name\": \"" + VERSION_ID + "\",\n"
                        + "      \"lastVersionId\": \"" + VERSION_ID + "\",\n"
                        + "      \"type\": \"custom\",\n"
                        + "      \"icon\": \"Furnace\"\n"
                        + "    }";

        String afterBrace = content.substring(braceIdx + 1).trim();
        String separator = "\n    ";
        String ending = afterBrace.startsWith("}") ? "\n" : ",\n";

        String updated = content.substring(0, braceIdx + 1)
                + separator + profileEntry + ending
                + content.substring(braceIdx + 1);

        Files.write(profilesFile.toPath(), updated.getBytes(StandardCharsets.UTF_8));
        ModLogger.info("Added launcher profile.");
    }
}
