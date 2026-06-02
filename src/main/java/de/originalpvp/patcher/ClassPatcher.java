package de.originalpvp.patcher;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * ASM-based bytecode patcher that transforms vanilla Minecraft 1.8.9 classes
 * and injects the OriginalPvP mod classes into the game JAR.
 */
public class ClassPatcher implements Opcodes {

    // Internal names of classes we patch
    private static final String MINECRAFT_CLASS = "ave";
    private static final String ENTITY_RENDERER_CLASS = "bfk";
    private static final String RENDER_GLOBAL_CLASS = "bfr";
    private static final String GUI_OPTIONS_CLASS = "axn";
    private static final String INVENTORY_PLAYER_CLASS = "wm";
    private static final String GUI_OVERLAY_DEBUG_CLASS = "avv";

    // Our hook target
    private static final String HOOK_CLASS = "de/originalpvp/OriginalPvP";

    // ─────────────────────────────────────────────────────────────────────
    //  Public entry point
    // ─────────────────────────────────────────────────────────────────────

    public static void patchMinecraftJar(String jarPath) throws Exception {
        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            System.err.println("[OriginalPvP] File not found: " + jarPath);
            return;
        }

        System.out.println("[OriginalPvP] Patching Minecraft JAR: " + jarPath);

        // 1. Create backup
        File backupFile = new File(jarPath + ".backup");
        if (!backupFile.exists()) {
            System.out.println("[OriginalPvP] Creating backup: " + backupFile.getName());
            Files.copy(jarFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            System.out.println("[OriginalPvP] Backup already exists, skipping.");
        }

        // 2. Read all entries from the JAR
        Map<String, byte[]> entries = new LinkedHashMap<>();
        Manifest manifest;
        try (JarFile jar = new JarFile(jarFile)) {
            manifest = jar.getManifest();
            Enumeration<JarEntry> jarEntries = jar.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry entry = jarEntries.nextElement();
                if (entry.isDirectory()) {
                    entries.put(entry.getName(), null);
                    continue;
                }
                String name = entry.getName();
                // 3. Remove signature files
                if (name.startsWith("META-INF/") && (name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA"))) {
                    System.out.println("[OriginalPvP] Removing signature: " + name);
                    continue;
                }
                entries.put(name, readStream(jar.getInputStream(entry)));
            }
        }

        // 4. Patch classes
        applyPatches(entries);

        // 5. Inject mod classes and ASM library from our own JAR
        injectModClasses(entries);

        // 6. Write the patched JAR
        System.out.println("[OriginalPvP] Writing patched JAR...");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))) {
            for (Map.Entry<String, byte[]> e : entries.entrySet()) {
                ZipEntry ze = new ZipEntry(e.getKey());
                jos.putNextEntry(ze);
                if (e.getValue() != null) {
                    jos.write(e.getValue());
                }
                jos.closeEntry();
            }
        }

        System.out.println("[OriginalPvP] Patching complete! You can now launch Minecraft 1.8.9.");
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Public API for InstallerLogic and build-time patching
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Applies all ASM patches to the vanilla classes present in the entry map.
     * Called by both the installer and the legacy {@code patchMinecraftJar} flow.
     */
    public static void applyPatches(Map<String, byte[]> entries) {
        patchEntry(entries, MINECRAFT_CLASS + ".class", "Minecraft");
        patchEntry(entries, ENTITY_RENDERER_CLASS + ".class", "EntityRenderer");
        patchEntry(entries, RENDER_GLOBAL_CLASS + ".class", "RenderGlobal");
        patchEntry(entries, GUI_OPTIONS_CLASS + ".class", "GuiOptions");
        patchEntry(entries, INVENTORY_PLAYER_CLASS + ".class", "InventoryPlayer");
        patchEntry(entries, GUI_OVERLAY_DEBUG_CLASS + ".class", "GuiOverlayDebug");
    }

    /**
     * Build-time helper: reads target vanilla classes from a Minecraft JAR,
     * applies ASM patches, and writes the patched {@code .class} files to an
     * output directory.  The patched files are then included in the shadow JAR
     * so that PrismLauncher's "Add to Minecraft JAR" works without an installer.
     */
    public static void extractPatchedClasses(String vanillaJarPath, String outputDir) throws Exception {
        System.out.println("[OriginalPvP] Extracting patched vanilla classes...");
        File vanillaFile = new File(vanillaJarPath);
        if (!vanillaFile.exists()) {
            System.out.println("[OriginalPvP] Vanilla JAR not found at " + vanillaJarPath +
                    " – skipping build-time patching.");
            return;
        }

        String[] targets = {
                MINECRAFT_CLASS, ENTITY_RENDERER_CLASS,
                RENDER_GLOBAL_CLASS, GUI_OPTIONS_CLASS,
                INVENTORY_PLAYER_CLASS, GUI_OVERLAY_DEBUG_CLASS
        };

        Map<String, byte[]> entries = new LinkedHashMap<String, byte[]>();
        try (JarFile jar = new JarFile(vanillaFile)) {
            for (String className : targets) {
                String entryName = className + ".class";
                JarEntry entry = jar.getJarEntry(entryName);
                if (entry != null) {
                    entries.put(entryName, readStream(jar.getInputStream(entry)));
                } else {
                    System.out.println("[OriginalPvP] WARNING: " + entryName + " not found in vanilla JAR.");
                }
            }
        }

        applyPatches(entries);

        File outDir = new File(outputDir);
        for (Map.Entry<String, byte[]> e : entries.entrySet()) {
            File outFile = new File(outDir, e.getKey());
            outFile.getParentFile().mkdirs();
            Files.write(outFile.toPath(), e.getValue());
            System.out.println("[OriginalPvP]   → " + e.getKey());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Dispatch to per-class patchers
    // ─────────────────────────────────────────────────────────────────────

    private static void patchEntry(Map<String, byte[]> entries, String entryName, String friendlyName) {
        byte[] original = entries.get(entryName);
        if (original == null) {
            System.err.println("[OriginalPvP] WARNING: Could not find " + entryName + " in JAR");
            return;
        }
        System.out.println("[OriginalPvP] Patching " + friendlyName + "...");
        byte[] patched;
        switch (entryName) {
            case MINECRAFT_CLASS + ".class":
                patched = patchMinecraftClass(original);
                break;
            case ENTITY_RENDERER_CLASS + ".class":
                patched = patchEntityRenderer(original);
                break;
            case RENDER_GLOBAL_CLASS + ".class":
                patched = patchRenderGlobal(original);
                break;
            case GUI_OPTIONS_CLASS + ".class":
                patched = patchGuiOptions(original);
                break;
            case INVENTORY_PLAYER_CLASS + ".class":
                patched = patchInventoryPlayer(original);
                break;
            case GUI_OVERLAY_DEBUG_CLASS + ".class":
                patched = patchGuiOverlayDebug(original);
                break;
            default:
                return;
        }
        entries.put(entryName, patched);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Inject our own classes into the target JAR
    // ─────────────────────────────────────────────────────────────────────

    private static void injectModClasses(Map<String, byte[]> entries) throws Exception {
        URI sourceUri = ClassPatcher.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        File sourceFile = new File(sourceUri);

        if (sourceFile.isFile() && sourceFile.getName().endsWith(".jar")) {
            // Running from a JAR – copy relevant entries
            try (JarFile ourJar = new JarFile(sourceFile)) {
                Enumeration<JarEntry> jarEntries = ourJar.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry entry = jarEntries.nextElement();
                    if (entry.isDirectory()) continue;
                    String name = entry.getName();
                    // Include mod classes and relocated ASM classes
                    if (name.startsWith("de/originalpvp/") || name.startsWith("org/objectweb/asm/") || name.startsWith("de/originalpvp/lib/asm/")) {
                        entries.put(name, readStream(ourJar.getInputStream(entry)));
                    }
                }
            }
            System.out.println("[OriginalPvP] Injected mod classes from JAR.");
        } else if (sourceFile.isDirectory()) {
            // Running from exploded classes directory (IDE / dev mode)
            injectFromDirectory(sourceFile, sourceFile, entries);
            System.out.println("[OriginalPvP] Injected mod classes from directory.");
        }
    }

    private static void injectFromDirectory(File root, File dir, Map<String, byte[]> entries) throws IOException {
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File child : children) {
            if (child.isDirectory()) {
                injectFromDirectory(root, child, entries);
            } else {
                String relative = root.toPath().relativize(child.toPath()).toString().replace('\\', '/');
                if (relative.startsWith("de/originalpvp/") || relative.startsWith("org/objectweb/asm/") || relative.startsWith("de/originalpvp/lib/asm/")) {
                    entries.put(relative, Files.readAllBytes(child.toPath()));
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  (a) Patch net.minecraft.client.Minecraft
    // ─────────────────────────────────────────────────────────────────────

    private static byte[] patchMinecraftClass(byte[] classBytes) {
        ClassReader cr = new ClassReader(classBytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                if ("am".equals(name) && "()V".equals(descriptor)) {
                    return new MethodVisitor(ASM9, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == RETURN) {
                                // Inject OriginalPvP.init() right before RETURN
                                mv.visitMethodInsn(INVOKESTATIC, HOOK_CLASS, "init", "()V", false);
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }

                if ("s".equals(name) && "()V".equals(descriptor)) {
                    return new MethodVisitor(ASM9, mv) {
                        @Override
                        public void visitCode() {
                            super.visitCode();
                            // Inject OriginalPvP.onTick() at the start of runTick
                            mv.visitMethodInsn(INVOKESTATIC, HOOK_CLASS, "onTick", "()V", false);
                        }
                    };
                }

                return mv;
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  (b) Patch net.minecraft.client.renderer.EntityRenderer
    // ─────────────────────────────────────────────────────────────────────

    private static byte[] patchEntityRenderer(byte[] classBytes) {
        ClassReader cr = new ClassReader(classBytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                // ── getFOVModifier(FZ)F -> a(FZ)F ──────────────────────────────────
                if ("a".equals(name) && "(FZ)F".equals(descriptor)) {
                    return new MethodVisitor(ASM9, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == FRETURN) {
                                // Stack has the float return value on top.
                                // We need: hookGetFOVModifier(originalFov, partialTicks)
                                mv.visitVarInsn(FLOAD, 1); // partialTicks
                                mv.visitVarInsn(ILOAD, 2); // useFOVSetting
                                mv.visitMethodInsn(INVOKESTATIC, HOOK_CLASS,
                                        "hookGetFOVModifier", "(FFZ)F", false);
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }

                // ── orientCamera(F)V -> f(F)V ─────────────────────────────────────
                if ("f".equals(name) && "(F)V".equals(descriptor)) {
                    return new MethodVisitor(ASM9, mv) {
                        @Override
                        public void visitFieldInsn(int opcode, String owner, String fieldName,
                                                   String fieldDescriptor) {
                            super.visitFieldInsn(opcode, owner, fieldName, fieldDescriptor);
                            // After GETFIELD GameSettings.viewBobbing Z -> avh.d
                            if (opcode == GETFIELD
                                    && "d".equals(fieldName)
                                    && "Z".equals(fieldDescriptor)) {
                                // AND with our hook result
                                mv.visitMethodInsn(INVOKESTATIC, HOOK_CLASS,
                                        "hookShouldApplyBobbing", "()Z", false);
                                mv.visitInsn(IAND);
                            }
                        }
                    };
                }

                // ── setupFog(IF)V -> a(IF)V ────────────────────────────────────────
                if ("a".equals(name) && "(IF)V".equals(descriptor)) {
                    return new MethodVisitor(ASM9, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == RETURN) {
                                mv.visitMethodInsn(INVOKESTATIC, HOOK_CLASS,
                                        "hookSetupFog", "()V", false);
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }

                // ── updateCameraAndRender(FJ)V -> a(FJ)V ──────────────────────────
                if ("a".equals(name) && "(FJ)V".equals(descriptor)) {
                    return new MethodVisitor(ASM9, mv) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String methodName,
                                                    String desc, boolean itf) {
                            super.visitMethodInsn(opcode, owner, methodName, desc, itf);
                            // After GuiIngame.renderGameOverlay(F)V -> avo.a(F)V
                            if ("a".equals(methodName) && "(F)V".equals(desc)) {
                                mv.visitVarInsn(FLOAD, 1); // partialTicks
                                mv.visitMethodInsn(INVOKESTATIC, HOOK_CLASS,
                                        "onRenderOverlay", "(F)V", false);
                            }
                        }
                    };
                }

                return mv;
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  (c) Patch net.minecraft.client.renderer.RenderGlobal
    // ─────────────────────────────────────────────────────────────────────

    private static byte[] patchRenderGlobal(byte[] classBytes) {
        ClassReader cr = new ClassReader(classBytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                if ("a".equals(name) && "(FI)V".equals(descriptor)) {
                    return new MethodVisitor(ASM9, mv) {
                        @Override
                        public void visitCode() {
                            super.visitCode();
                            Label continueLabel = new Label();
                            mv.visitMethodInsn(INVOKESTATIC, HOOK_CLASS,
                                    "hookShouldRenderSky", "()Z", false);
                            mv.visitJumpInsn(IFNE, continueLabel); // if true, continue
                            mv.visitInsn(RETURN);                  // else skip
                            mv.visitLabel(continueLabel);
                        }
                    };
                }

                return mv;
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  (d) Patch net.minecraft.client.gui.GuiOptions
    // ─────────────────────────────────────────────────────────────────────

    private static byte[] patchGuiOptions(byte[] classBytes) {
        ClassReader cr = new ClassReader(classBytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                if ("b".equals(name) && "()V".equals(descriptor)) {
                    return new MethodVisitor(ASM9, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == RETURN) {
                                mv.visitVarInsn(ALOAD, 0); // this
                                mv.visitFieldInsn(GETFIELD, "axu", "n", "Ljava/util/List;");
                                mv.visitTypeInsn(NEW, "avs");
                                mv.visitInsn(DUP);
                                mv.visitIntInsn(SIPUSH, 999);  // id
                                mv.visitVarInsn(ALOAD, 0);
                                mv.visitFieldInsn(GETFIELD, "axn", "l", "I"); // width
                                mv.visitIntInsn(BIPUSH, 125);
                                mv.visitInsn(ISUB);
                                mv.visitVarInsn(ALOAD, 0);
                                mv.visitFieldInsn(GETFIELD, "axn", "m", "I"); // height
                                mv.visitIntInsn(BIPUSH, 25);
                                mv.visitInsn(ISUB);
                                mv.visitIntInsn(BIPUSH, 120); // w
                                mv.visitIntInsn(BIPUSH, 20);  // h
                                mv.visitLdcInsn("Original PvP Settings");
                                mv.visitMethodInsn(INVOKESPECIAL, "avs", "<init>", "(IIIIILjava/lang/String;)V", false);
                                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
                                mv.visitInsn(POP);
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }

                if ("a".equals(name) && "(Lavs;)V".equals(descriptor)) {
                    return new MethodVisitor(ASM9, mv) {
                        @Override
                        public void visitCode() {
                            super.visitCode();
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitFieldInsn(GETFIELD, "avs", "k", "I"); // button.id
                            mv.visitIntInsn(SIPUSH, 999);
                            Label continueLabel = new Label();
                            mv.visitJumpInsn(IF_ICMPNE, continueLabel);

                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, "axn", "j", "Lave;"); // mc
                            mv.visitTypeInsn(NEW, "de/originalpvp/gui/GuiModMenu");
                            mv.visitInsn(DUP);
                            mv.visitVarInsn(ALOAD, 0); // parent screen
                            mv.visitMethodInsn(INVOKESPECIAL, "de/originalpvp/gui/GuiModMenu", "<init>", "(Laxu;)V", false); // GuiScreen
                            mv.visitMethodInsn(INVOKEVIRTUAL, "ave", "a", "(Laxu;)V", false); // displayGuiScreen
                            mv.visitInsn(RETURN);
                            mv.visitLabel(continueLabel);
                        }
                    };
                }

                return mv;
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  (e) Patch net.minecraft.client.gui.GuiOverlayDebug
    // ─────────────────────────────────────────────────────────────────────

    private static byte[] patchGuiOverlayDebug(byte[] classBytes) {
        ClassReader cr = new ClassReader(classBytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                if ("b".equals(name) && "()Ljava/util/List;".equals(descriptor)) {
                    return new MethodVisitor(ASM9, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == ARETURN) {
                                // Stack has the List<String> at top.
                                // Duplicate it and pass to our hook.
                                mv.visitInsn(DUP);
                                mv.visitMethodInsn(INVOKESTATIC, HOOK_CLASS, "hookF3Menu", "(Ljava/util/List;)V", false);
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }
                return mv;
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    private static byte[] patchInventoryPlayer(byte[] classBytes) {
        ClassReader cr = new ClassReader(classBytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                // InventoryPlayer.changeCurrentItem(int direction)
                if ("d".equals(name) && "(I)V".equals(descriptor)) {
                    return new MethodVisitor(ASM9, mv) {
                        @Override
                        public void visitCode() {
                            super.visitCode();
                            mv.visitMethodInsn(INVOKESTATIC, HOOK_CLASS, "hookCancelItemScroll", "()Z", false);
                            Label continueScroll = new Label();
                            mv.visitJumpInsn(IFEQ, continueScroll);
                            mv.visitInsn(RETURN);
                            mv.visitLabel(continueScroll);
                        }
                    };
                }

                return mv;
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    private static byte[] readStream(java.io.InputStream is) throws java.io.IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len;
        while ((len = is.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        is.close();
        return baos.toByteArray();
    }
}
