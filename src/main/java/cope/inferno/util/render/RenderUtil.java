package cope.inferno.util.render;

import cope.inferno.util.internal.Wrapper;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtil implements Wrapper {
    /**
     * Gets the scaled resolution of the game window
     * @return The ScaledResolution object
     */
    public static ScaledResolution getResolution() {
        return new ScaledResolution(mc);
    }

    /**
     * Draws a 2D rectangle
     * @param x The x value
     * @param y The y value
     * @param w The width
     * @param h The height
     * @param color The color using hex
     */
    public static void drawRectangle(double x, double y, double w, double h, int color) {
        float[] rgba = ColorUtil.getColor(color);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y, 0.0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(x, y + h, 0.0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(x + w, y + h, 0.0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(x + w, y, 0.0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(GL_FLAT);
        GlStateManager.enableTexture2D();
    }

    /**
     * Draws a standalone outline
     * @param x The x value
     * @param y The y value
     * @param w The width
     * @param h The height
     * @param lineWidth The width of the lines
     * @param color The color of the lines
     */
    public static void drawOutline(double x, double y, double w, double h, float lineWidth, int color) {
        drawLine(x, y, x + w, y, lineWidth, color); // top
        drawLine(x, y + h, x + w, y + h, lineWidth, color); // bottom
        drawLine(x, y, x, y + h, lineWidth, color);  // left
        drawLine(x + w, y, x + w, y + h, lineWidth, color); // right
    }

    /**
     * Draws a line
     * @param startX The x value to begin at
     * @param startY The y value to begin at
     * @param endX The x value to end at
     * @param endY The y value to end at
     * @param lineWidth The width of the line
     * @param color The color of the line
     */
    public static void drawLine(double startX, double startY, double endX, double endY, float lineWidth, int color) {
        float[] rgba = ColorUtil.getColor(color);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();

        GlStateManager.glLineWidth(lineWidth);

        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(startX, startY, 0.0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(endX, endY, 0.0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
    }

    /**
     * Cuts out render elements from some bounds <a href="https://www.khronos.org/opengl/wiki/Scissor_Test">glScissor</a>
     * @param x The x value
     * @param y The y value
     * @param w The width
     * @param h The height
     */
    public static void startScissor(int x, int y, int w, int h) {
        ScaledResolution res = getResolution();
        int scaleFactor = res.getScaleFactor();

        glEnable(GL_SCISSOR_TEST);
        glScissor(x * scaleFactor, (res.getScaledHeight() - h) * scaleFactor, (w - x) * scaleFactor, (h - y) * scaleFactor);
    }

    /**
     * Disables the scissor test if it has been enabled
     */
    public static void stopScissor() {
        glDisable(GL_SCISSOR_TEST);
    }
}
