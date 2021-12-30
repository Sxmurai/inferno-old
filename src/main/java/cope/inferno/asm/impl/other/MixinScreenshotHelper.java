package cope.inferno.asm.impl.other;

import cope.inferno.core.features.module.other.FullScreenshot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ScreenShotHelper;
import org.lwjgl.BufferUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

@Mixin(ScreenShotHelper.class)
public class MixinScreenshotHelper {
    @Shadow public static IntBuffer pixelBuffer;
    @Shadow private static int[] pixelValues;

    @Overwrite
    public static BufferedImage createScreenshot(int width, int height, Framebuffer framebufferIn) {
        if (FullScreenshot.INSTANCE.isToggled()) {
            try {
                return new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            } catch (AWTException e) {
                System.out.println("Screenshot failed to be taken, resorting to vanilla screenshot...");
                return defaultCreateScreenshot(width, height, framebufferIn);
            }
        } else {
            return defaultCreateScreenshot(width, height, framebufferIn);
        }
    }

    private static BufferedImage defaultCreateScreenshot(int width, int height, Framebuffer framebufferIn) {
        if (OpenGlHelper.isFramebufferEnabled()) {
            width = framebufferIn.framebufferTextureWidth;
            height = framebufferIn.framebufferTextureHeight;
        }

        int i = width * height;
        if (pixelBuffer == null || pixelBuffer.capacity() < i) {
            pixelBuffer = BufferUtils.createIntBuffer(i);
            pixelValues = new int[i];
        }

        GlStateManager.glPixelStorei(3333, 1);
        GlStateManager.glPixelStorei(3317, 1);

        pixelBuffer.clear();

        if (OpenGlHelper.isFramebufferEnabled()) {
            GlStateManager.bindTexture(framebufferIn.framebufferTexture);
            GlStateManager.glGetTexImage(3553, 0, 32993, 33639, pixelBuffer);
        } else {
            GlStateManager.glReadPixels(0, 0, width, height, 32993, 33639, pixelBuffer);
        }

        pixelBuffer.get(pixelValues);
        TextureUtil.processPixelValues(pixelValues, width, height);

        BufferedImage bufferedimage = new BufferedImage(width, height, 1);
        bufferedimage.setRGB(0, 0, width, height, pixelValues, 0, width);

        return bufferedimage;
    }
}
