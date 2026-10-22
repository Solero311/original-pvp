package de.originalpvp.patcher.transformers;

import de.originalpvp.patcher.AbstractClassTransformer;
import de.originalpvp.patcher.VanillaMappings;
import org.objectweb.asm.MethodVisitor;

/**
 * Instruments {@code net.minecraft.client.renderer.EntityRenderer} ({@code bfk}):
 * <ul>
 *   <li>Hooks {@code getFOVModifier(FZ)F} ({@code a(FZ)F}) for Zoom and FOV Effects.</li>
 *   <li>Hooks {@code orientCamera(F)V} ({@code f(F)V}) for View Bobbing control.</li>
 *   <li>Hooks {@code setupFog(IF)V} ({@code a(IF)V}) for Fog suppression.</li>
 *   <li>Hooks {@code updateCameraAndRender(FJ)V} ({@code a(FJ)V}) for HUD overlay rendering.</li>
 * </ul>
 */
public class EntityRendererTransformer extends AbstractClassTransformer {

    public EntityRendererTransformer() {
        super(VanillaMappings.ENTITY_RENDERER_CLASS, "EntityRenderer");
    }

    @Override
    protected MethodVisitor transformMethod(MethodVisitor mv, int access, String name, String descriptor) {
        // getFOVModifier(FZ)F -> a(FZ)F
        if (VanillaMappings.ER_GET_FOV_MODIFIER_NAME.equals(name)
                && VanillaMappings.ER_GET_FOV_MODIFIER_DESC.equals(descriptor)) {
            return new MethodVisitor(ASM9, mv) {
                @Override
                public void visitInsn(int opcode) {
                    if (opcode == FRETURN) {
                        mv.visitVarInsn(FLOAD, 1); // partialTicks
                        mv.visitVarInsn(ILOAD, 2); // useFOVSetting
                        mv.visitMethodInsn(INVOKESTATIC, VanillaMappings.HOOK_CLASS,
                                "hookGetFOVModifier", "(FFZ)F", false);
                    }
                    super.visitInsn(opcode);
                }
            };
        }

        // orientCamera(F)V -> f(F)V
        if (VanillaMappings.ER_ORIENT_CAMERA_NAME.equals(name)
                && VanillaMappings.ER_ORIENT_CAMERA_DESC.equals(descriptor)) {
            return new MethodVisitor(ASM9, mv) {
                @Override
                public void visitFieldInsn(int opcode, String owner, String fieldName, String fieldDescriptor) {
                    super.visitFieldInsn(opcode, owner, fieldName, fieldDescriptor);
                    if (opcode == GETFIELD
                            && VanillaMappings.GAME_SETTINGS_VIEW_BOBBING_FIELD.equals(fieldName)
                            && VanillaMappings.BOOLEAN_DESC.equals(fieldDescriptor)) {
                        mv.visitMethodInsn(INVOKESTATIC, VanillaMappings.HOOK_CLASS,
                                "hookShouldApplyBobbing", "()Z", false);
                        mv.visitInsn(IAND);
                    }
                }
            };
        }

        // setupFog(IF)V -> a(IF)V
        if (VanillaMappings.ER_SETUP_FOG_NAME.equals(name)
                && VanillaMappings.ER_SETUP_FOG_DESC.equals(descriptor)) {
            return new MethodVisitor(ASM9, mv) {
                @Override
                public void visitInsn(int opcode) {
                    if (opcode == RETURN) {
                        mv.visitMethodInsn(INVOKESTATIC, VanillaMappings.HOOK_CLASS,
                                "hookSetupFog", "()V", false);
                    }
                    super.visitInsn(opcode);
                }
            };
        }

        // updateCameraAndRender(FJ)V -> a(FJ)V
        if (VanillaMappings.ER_UPDATE_CAMERA_AND_RENDER_NAME.equals(name)
                && VanillaMappings.ER_UPDATE_CAMERA_AND_RENDER_DESC.equals(descriptor)) {
            return new MethodVisitor(ASM9, mv) {
                @Override
                public void visitMethodInsn(int opcode, String owner, String methodName, String desc, boolean itf) {
                    super.visitMethodInsn(opcode, owner, methodName, desc, itf);
                    if (VanillaMappings.GUI_INGAME_RENDER_OVERLAY_NAME.equals(methodName)
                            && VanillaMappings.GUI_INGAME_RENDER_OVERLAY_DESC.equals(desc)) {
                        mv.visitVarInsn(FLOAD, 1); // partialTicks
                        mv.visitMethodInsn(INVOKESTATIC, VanillaMappings.HOOK_CLASS,
                                "onRenderOverlay", "(F)V", false);
                    }
                }
            };
        }

        return mv;
    }
}
