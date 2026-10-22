package de.originalpvp.patcher.transformers;

import de.originalpvp.patcher.AbstractClassTransformer;
import de.originalpvp.patcher.VanillaMappings;
import org.objectweb.asm.MethodVisitor;

/**
 * Instruments {@code net.minecraft.client.gui.GuiOverlayDebug} ({@code avv}):
 * <ul>
 *   <li>Injects Original PvP version information into the F3 debug screen in {@code call()} ({@code b()Ljava/util/List;}).</li>
 * </ul>
 */
public class GuiOverlayDebugTransformer extends AbstractClassTransformer {

    public GuiOverlayDebugTransformer() {
        super(VanillaMappings.GUI_OVERLAY_DEBUG_CLASS, "GuiOverlayDebug");
    }

    @Override
    protected MethodVisitor transformMethod(MethodVisitor mv, int access, String name, String descriptor) {
        if (VanillaMappings.GOD_CALL_NAME.equals(name)
                && VanillaMappings.GOD_CALL_DESC.equals(descriptor)) {
            return new MethodVisitor(ASM9, mv) {
                @Override
                public void visitInsn(int opcode) {
                    if (opcode == ARETURN) {
                        mv.visitInsn(DUP);
                        mv.visitMethodInsn(INVOKESTATIC, VanillaMappings.HOOK_CLASS,
                                "hookF3Menu", "(Ljava/util/List;)V", false);
                    }
                    super.visitInsn(opcode);
                }
            };
        }
        return mv;
    }
}
