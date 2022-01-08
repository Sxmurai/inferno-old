package cope.inferno.core.gui.font;

import cope.inferno.util.internal.Wrapper;
import cope.inferno.util.render.ColorUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class CustomFontRenderer extends FontRenderer implements Wrapper {
    private final AWTFont font;
    private final int[] colorCodes = new int[32];

    public CustomFontRenderer(Font font) {
        super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.renderEngine, false);

        // create our bitmap texture and our character map
        this.font = new AWTFont(font);

        // setup mc color codes for use in strings
        setupMcColorCodes();

        // set our font height
        FONT_HEIGHT = font.getSize() - 9;
    }

    @Override
    public int drawString(String text, int x, int y, int color) {
        return drawString(text, x, y, color, false);
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        int width = drawString(text, x + 1.0f, y + 1.0f, color, true);
        return Math.min(width, drawString(text, x, y, color, false));
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        if (color == 0x20FFFFFF) {
            color = 0xFFFFFF;
        }

        if ((color & 0xFC000000) == 0) {
            color |= 0xFF000000;
        }

        if (dropShadow) {
            color = (color & 0xFCFCFC) >> 2 | color & 0xFF000000;
        }

        double x1 = x - 1.5;
        double y1 = y - 0.5;

        x1 *= 2.0;
        y1 = (y1 - 3.0) * 2.0;

        float[] rgba = ColorUtil.getColor(color);

        glPushMatrix();
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(rgba[0], rgba[1], rgba[2], rgba[3]);

        // bind textures
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(font.getTexture().getGlTextureId());
        glBindTexture(3553, font.getTexture().getGlTextureId());

        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);

            CharData charData = font.getChar(c);
            if (charData == null) {
                continue;
            }

            if (c == '\u00a7') {
                int colorCode = 21;
                try {
                    colorCode = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
                } catch (Exception ignored) { }

                if (colorCode < 16) {
                    if (colorCode < 0 || colorCode > 15) {
                        colorCode = 15;
                    }

                    if (dropShadow) {
                        colorCode += 16;
                    }

                    colorCode = colorCodes[colorCode];
                    GlStateManager.color((float) (colorCode >> 16 & 0xFF) / 255.0f, (float) (colorCode >> 8 & 0xFF) / 255.0f, (float) (colorCode & 0xFF) / 255.0f, rgba[3]);
                }

                ++i;
                continue;
            }

            font.drawChar(charData, (float) x1, (float) y1);

            x1 += charData.getWidth() - 0.5f;
        }

        glPopMatrix();

        return (int) (x1 / 2.0);
    }

    @Override
    public int getStringWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);

            CharData charData = font.getChar(c);
            if (charData == null) {
                continue;
            }

            if (c == '\u00a7') {
                ++i;
                continue; // ignore color codes
            }

            width += charData.getWidth();
        }

        return width / 2;
    }

    private void setupMcColorCodes() {
        for (int index = 0; index < 32; ++index) {
            int noClue = (index >> 3 & 1) * 85;
            int red = (index >> 2 & 1) * 170 + noClue;
            int green = (index >> 1 & 1) * 170 + noClue;
            int blue = (index & 1) * 170 + noClue;

            if (index == 6) {
                red += 85;
            }

            if (index >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            colorCodes[index] = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
        }
    }
}
