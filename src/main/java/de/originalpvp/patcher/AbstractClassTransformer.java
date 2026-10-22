package de.originalpvp.patcher;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Base class for ASM bytecode transformers that simplifies method-level instrumentation.
 */
public abstract class AbstractClassTransformer implements IClassTransformer, Opcodes {

    private final String targetClassName;
    private final String friendlyName;

    protected AbstractClassTransformer(String targetClassName, String friendlyName) {
        this.targetClassName = targetClassName;
        this.friendlyName = friendlyName;
    }

    @Override
    public String getTargetClassName() {
        return targetClassName;
    }

    @Override
    public String getFriendlyName() {
        return friendlyName;
    }

    @Override
    public byte[] transform(byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new ClassVisitor(ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor baseVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                return transformMethod(baseVisitor, access, name, descriptor);
            }
        };
        reader.accept(visitor, 0);
        return writer.toByteArray();
    }

    /**
     * Override in subclasses to wrap {@code baseVisitor} for specific methods.
     */
    protected abstract MethodVisitor transformMethod(MethodVisitor baseVisitor, int access, String name, String descriptor);
}
