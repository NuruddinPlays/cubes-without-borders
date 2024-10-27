package dev.kir.cubeswithoutborders.client.mixin;

import dev.kir.cubeswithoutborders.client.ResizableGameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = Framebuffer.class, priority = 0)
abstract class FramebufferMixin {
    @ModifyArg(method = "*", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texParameter(III)V"), index = 2)
    private int setTexFilter(int target, int pname, int param) {
        if (pname == GL12.GL_TEXTURE_MAG_FILTER) {
            return GL12.GL_LINEAR;
        }

        if (pname == GL12.GL_TEXTURE_WRAP_S || pname == GL12.GL_TEXTURE_WRAP_T) {
            return GL12.GL_CLAMP_TO_EDGE;
        }

        return param;
    }

    @Inject(method = "draw(IIZ)V", at = @At(value = "HEAD"), cancellable = true)
    private void forceDraw(int width, int height, boolean disableBlend, CallbackInfo ci) {
        // When blending is not used, Sodium bypasses the standard rendering
        // process and directly copies the contents of one framebuffer to
        // another, throwing our scaling logic under the bus.
        // Therefore, we need to intervene before Sodium has the opportunity
        // to do anything fancy.
        if (ResizableGameRenderer.getInstance().isRendering()) {
            this.drawInternal(width, height, disableBlend);
            ci.cancel();
        }
    }

    @Shadow
    protected abstract void drawInternal(int width, int height, boolean disableBlend);
}
