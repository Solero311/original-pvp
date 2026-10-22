package de.originalpvp.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Shared utility methods for reading, modifying, and writing JAR archives.
 */
public final class JarUtils {

    private static final int BUFFER_SIZE = 8192;

    private JarUtils() {
        // Prevent instantiation
    }

    /**
     * Reads all bytes from the given stream without closing the underlying container prematurely,
     * then closes the entry stream.
     */
    public static byte[] readStream(InputStream is) throws IOException {
        try (InputStream in = is; ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            return baos.toByteArray();
        }
    }

    /**
     * Reads all entries of a JAR file into an ordered map.
     *
     * @param jarFile         the source JAR file
     * @param stripMetaInfAll if true, removes all META-INF/ entries; otherwise only strips signature files (.SF/.RSA/.DSA)
     * @return ordered map of entry names to their byte contents (null for directories)
     */
    public static Map<String, byte[]> readJarEntries(File jarFile, boolean stripMetaInfAll) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<String, byte[]>();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> enumeration = jar.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry entry = enumeration.nextElement();
                String name = entry.getName();

                if (stripMetaInfAll && name.startsWith("META-INF/")) {
                    continue;
                }
                if ( isSignatureFile(name)) {
                    ModLogger.info("Removing signature: " + name);
                    continue;
                }

                if (entry.isDirectory()) {
                    entries.put(name, null);
                } else {
                    entries.put(name, readStream(jar.getInputStream(entry)));
                }
            }
        }
        return entries;
    }

    /**
     * Writes a map of JAR entries to the specified target JAR file.
     */
    public static void writeJarEntries(File targetJar, Map<String, byte[]> entries) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(targetJar))) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                jos.putNextEntry(zipEntry);
                if (entry.getValue() != null) {
                    jos.write(entry.getValue());
                }
                jos.closeEntry();
            }
        }
    }

    /**
     * Injects OriginalPvP mod classes and bundled ASM classes from the currently running
     * archive or exploded class directory into the target entry map.
     *
     * @param anchorClass      class used to locate the protection domain source
     * @param entries          target JAR entry map
     * @param excludeInstaller if true, omits {@code de/originalpvp/installer/*} classes
     */
    public static void injectModClasses(Class<?> anchorClass, Map<String, byte[]> entries, boolean excludeInstaller) throws Exception {
        URI sourceUri = anchorClass.getProtectionDomain().getCodeSource().getLocation().toURI();
        File sourceFile = new File(sourceUri);

        if (sourceFile.isFile() && sourceFile.getName().endsWith(".jar")) {
            try (JarFile ourJar = new JarFile(sourceFile)) {
                Enumeration<JarEntry> jarEntries = ourJar.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry entry = jarEntries.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    String name = entry.getName();
                    if (shouldInjectEntry(name, excludeInstaller)) {
                        entries.put(name, readStream(ourJar.getInputStream(entry)));
                    }
                }
            }
            ModLogger.info("Injected mod classes from JAR.");
        } else if (sourceFile.isDirectory()) {
            injectFromDirectory(sourceFile, sourceFile, entries, excludeInstaller);
            ModLogger.info("Injected mod classes from directory.");
        } else {
            ModLogger.warn("Unknown code source location: " + sourceFile.getAbsolutePath());
        }
    }

    private static void injectFromDirectory(File root, File currentDir, Map<String, byte[]> entries, boolean excludeInstaller) throws IOException {
        File[] children = currentDir.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                injectFromDirectory(root, child, entries, excludeInstaller);
            } else {
                String relative = root.toPath().relativize(child.toPath()).toString().replace('\\', '/');
                if (shouldInjectEntry(relative, excludeInstaller)) {
                    entries.put(relative, Files.readAllBytes(child.toPath()));
                }
            }
        }
    }

    private static boolean shouldInjectEntry(String name, boolean excludeInstaller) {
        if (name.startsWith("META-INF/")) {
            return false;
        }
        if (excludeInstaller && name.startsWith("de/originalpvp/installer/")) {
            return false;
        }
        return name.startsWith("de/originalpvp/")
                || name.startsWith("org/objectweb/asm/")
                || name.startsWith("de/originalpvp/lib/asm/");
    }

    private static boolean isSignatureFile(String name) {
        if (!name.startsWith("META-INF/")) {
            return false;
        }
        return name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA");
    }
}
