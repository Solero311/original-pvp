package de.originalpvp.patcher.transformers;

import de.originalpvp.patcher.AbstractClassTransformer;
import de.originalpvp.patcher.VanillaMappings;
import org.objectweb.asm.MethodVisitor;

/**
 * Instruments {@code net.minecraft.client.Minecraft} ({@code ave}):
 * <ul>
 *   <li>Injects {@code OriginalPvP.init()} before return in {@code startGame()} ({@code am()V}).</li>
 *   <li>Injects {@code OriginalPvP.onTick()} at the start of {@code runTick()} ({@code s()V}).</li>
 * </ul>
 */
public class MinecraftTransformer extends AbstractClassTransformer {

    public MinecraftTransformer() {
        super(VanillaMappings.MINECRAFT_CLASS, "Minecraft");
    }

    @Override
    protected MethodVisitor transformMethod(MethodVisitor mv, int access, String name, String descriptor) {
        if (VanillaMappings.MINECRAFT_START_GAME_NAME.equals(name)
                && VanillaMappings.MINECRAFT_START_GAME_DESC.equals(descriptor)) {
            return new MethodVisitor(ASM9, mv) {
                @Override
                public void visitInsn(int opcode) {
                    if (opcode == RETURN) {
                        mv.visitMethodInsn(INVOKESTATIC, VanillaMappings.HOOK_CLASS, "init", "()V", false);
                    }
                    super.visitInsn(opcode);
                }
            };
        }

        if (VanillaMappings.MINECRAFT_RUN_TICK_NAME.equals(name)
                && VanillaMappings.MINECRAFT_RUN_TICK_DESC.equals(descriptor)) {
            return new MethodVisitor(ASM9, mv) {
                @Override
                public void visitCode() {
                    super.visitCode();
                    mv.visitMethodInsn(INVOKESTATIC, VanillaMappings.HOOK_CLASS, "onTick", "()V", false);
                }
            };
        }

        return mv;
    }
}
