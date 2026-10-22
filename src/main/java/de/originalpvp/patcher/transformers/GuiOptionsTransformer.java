package de.originalpvp.patcher.transformers;

import de.originalpvp.patcher.AbstractClassTransformer;
import de.originalpvp.patcher.VanillaMappings;
import de.originalpvp.util.ModConstants;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Instruments {@code net.minecraft.client.gui.GuiOptions} ({@code axn}):
 * <ul>
 *   <li>Adds an "Original PvP Settings" button in {@code initGui()} ({@code b()V}).</li>
 *   <li>Opens {@code GuiModMenu} when the button is clicked in {@code actionPerformed(GuiButton)} ({@code a(Lavs;)V}).</li>
 * </ul>
 */
public class GuiOptionsTransformer extends AbstractClassTransformer {

    public GuiOptionsTransformer() {
        super(VanillaMappings.GUI_OPTIONS_CLASS, "GuiOptions");
    }

    @Override
    protected MethodVisitor transformMethod(MethodVisitor mv, int access, String name, String descriptor) {
        if (VanillaMappings.GUI_INIT_NAME.equals(name)
                && VanillaMappings.GUI_INIT_DESC.equals(descriptor)) {
            return new MethodVisitor(ASM9, mv) {
                @Override
                public void visitInsn(int opcode) {
                    if (opcode == RETURN) {
                        mv.visitVarInsn(ALOAD, 0); // this
                        mv.visitFieldInsn(GETFIELD, VanillaMappings.GUI_SCREEN_CLASS,
                                VanillaMappings.GUI_SCREEN_BUTTON_LIST_FIELD, "Ljava/util/List;");
                        mv.visitTypeInsn(NEW, VanillaMappings.GUI_BUTTON_CLASS);
                        mv.visitInsn(DUP);
                        mv.visitIntInsn(SIPUSH, ModConstants.OPTIONS_BUTTON_ID);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, VanillaMappings.GUI_OPTIONS_CLASS,
                                VanillaMappings.GUI_SCREEN_WIDTH_FIELD, "I");
                        mv.visitIntInsn(BIPUSH, 125);
                        mv.visitInsn(ISUB);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, VanillaMappings.GUI_OPTIONS_CLASS,
                                VanillaMappings.GUI_SCREEN_HEIGHT_FIELD, "I");
                        mv.visitIntInsn(BIPUSH, 25);
                        mv.visitInsn(ISUB);
                        mv.visitIntInsn(BIPUSH, 120); // width
                        mv.visitIntInsn(BIPUSH, 20);  // height
                        mv.visitLdcInsn("Original PvP Settings");
                        mv.visitMethodInsn(INVOKESPECIAL, VanillaMappings.GUI_BUTTON_CLASS,
                                "<init>", "(IIIIILjava/lang/String;)V", false);
                        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List",
                                "add", "(Ljava/lang/Object;)Z", true);
                        mv.visitInsn(POP);
                    }
                    super.visitInsn(opcode);
                }
            };
        }

        if (VanillaMappings.GUI_ACTION_PERFORMED_NAME.equals(name)
                && VanillaMappings.GUI_ACTION_PERFORMED_DESC.equals(descriptor)) {
            return new MethodVisitor(ASM9, mv) {
                @Override
                public void visitCode() {
                    super.visitCode();
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitFieldInsn(GETFIELD, VanillaMappings.GUI_BUTTON_CLASS,
                            VanillaMappings.GUI_BUTTON_ID_FIELD, "I");
                    mv.visitIntInsn(SIPUSH, ModConstants.OPTIONS_BUTTON_ID);
                    Label continueLabel = new Label();
                    mv.visitJumpInsn(IF_ICMPNE, continueLabel);

                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, VanillaMappings.GUI_OPTIONS_CLASS,
                            VanillaMappings.GUI_SCREEN_MC_FIELD, "L" + VanillaMappings.MINECRAFT_CLASS + ";");
                    mv.visitTypeInsn(NEW, VanillaMappings.GUI_MOD_MENU_CLASS);
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(ALOAD, 0); // parent screen
                    mv.visitMethodInsn(INVOKESPECIAL, VanillaMappings.GUI_MOD_MENU_CLASS,
                            "<init>", "(L" + VanillaMappings.GUI_SCREEN_CLASS + ";)V", false);
                    mv.visitMethodInsn(INVOKEVIRTUAL, VanillaMappings.MINECRAFT_CLASS,
                            VanillaMappings.MINECRAFT_DISPLAY_GUI_NAME,
                            VanillaMappings.MINECRAFT_DISPLAY_GUI_DESC, false);
                    mv.visitInsn(RETURN);
                    mv.visitLabel(continueLabel);
                }
            };
        }

        return mv;
    }
}
