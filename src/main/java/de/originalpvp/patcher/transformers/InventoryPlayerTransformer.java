package de.originalpvp.patcher.transformers;

import de.originalpvp.patcher.AbstractClassTransformer;
import de.originalpvp.patcher.VanillaMappings;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Instruments {@code net.minecraft.entity.player.InventoryPlayer} ({@code wm}):
 * <ul>
 *   <li>Cancels hotbar mouse-wheel scrolling in {@code changeCurrentItem(I)V} ({@code d(I)V}) while zooming.</li>
 * </ul>
 */
public class InventoryPlayerTransformer extends AbstractClassTransformer {

    public InventoryPlayerTransformer() {
        super(VanillaMappings.INVENTORY_PLAYER_CLASS, "InventoryPlayer");
    }

    @Override
    protected MethodVisitor transformMethod(MethodVisitor mv, int access, String name, String descriptor) {
        if (VanillaMappings.IP_CHANGE_CURRENT_item_NAME.equals(name)
                && VanillaMappings.IP_CHANGE_CURRENT_ITEM_DESC.equals(descriptor)) {
            return new MethodVisitor(ASM9, mv) {
                @Override
                public void visitCode() {
                    super.visitCode();
                    mv.visitMethodInsn(INVOKESTATIC, VanillaMappings.HOOK_CLASS,
                            "hookCancelItemScroll", "()Z", false);
                    Label continueScroll = new Label();
                    mv.visitJumpInsn(IFEQ, continueScroll);
                    mv.visitInsn(RETURN);
                    mv.visitLabel(continueScroll);
                }
            };
        }
        return mv;
    }
}
