package de.originalpvp.patcher.transformers;

import de.originalpvp.patcher.AbstractClassTransformer;
import de.originalpvp.patcher.VanillaMappings;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Instruments {@code net.minecraft.client.renderer.RenderGlobal} ({@code bfr}):
 * <ul>
 *   <li>Conditionally cancels {@code renderSky(FI)V} ({@code a(FI)V}) when Sky rendering is disabled.</li>
 * </ul>
 */
public class RenderGlobalTransformer extends AbstractClassTransformer {

    public RenderGlobalTransformer() {
        super(VanillaMappings.RENDER_GLOBAL_CLASS, "RenderGlobal");
    }

    @Override
    protected MethodVisitor transformMethod(MethodVisitor mv, int access, String name, String descriptor) {
        if (VanillaMappings.RG_RENDER_SKY_NAME.equals(name)
                && VanillaMappings.RG_RENDER_SKY_DESC.equals(descriptor)) {
            return new MethodVisitor(ASM9, mv) {
                @Override
                public void visitCode() {
                    super.visitCode();
                    Label continueLabel = new Label();
                    mv.visitMethodInsn(INVOKESTATIC, VanillaMappings.HOOK_CLASS,
                            "hookShouldRenderSky", "()Z", false);
                    mv.visitJumpInsn(IFNE, continueLabel);
                    mv.visitInsn(RETURN);
                    mv.visitLabel(continueLabel);
                }
            };
        }
        return mv;
    }
}
