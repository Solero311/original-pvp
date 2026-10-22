package de.originalpvp.util;

/**
 * Lightweight, dependency-free logger for Original PvP.
 * Works identically in standalone installer mode, build-patch mode, and in-game runtime.
 */
public final class ModLogger {

    private static final String PREFIX = "[OriginalPvP]";

    private ModLogger() {
        // Prevent instantiation
    }

    public static void info(String message) {
        System.out.println(PREFIX + " " + message);
    }

    public static void warn(String message) {
        System.out.println(PREFIX + " [WARN] " + message);
    }

    public static void error(String message) {
        System.err.println(PREFIX + " [ERROR] " + message);
    }

    public static void error(String message, Throwable throwable) {
        System.err.println(PREFIX + " [ERROR] " + message);
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }
}
