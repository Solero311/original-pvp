package de.originalpvp.patcher;

/**
 * Contract for an ASM bytecode transformer targeting a specific vanilla Minecraft class.
 */
public interface IClassTransformer {

    /**
     * @return the Notch-obfuscated internal class name (e.g., {@code "ave"})
     */
    String getTargetClassName();

    /**
     * @return the deobfuscated MCP class name for logging (e.g., {@code "Minecraft"})
     */
    String getFriendlyName();

    /**
     * @return the JAR entry path for the target class (e.g., {@code "ave.class"})
     */
    default String getEntryName() {
        return getTargetClassName() + ".class";
    }

    /**
     * Transforms the original class bytecode and returns the instrumented bytecode.
     */
    byte[] transform(byte[] classBytes);
}
