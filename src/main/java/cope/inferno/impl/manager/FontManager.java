package cope.inferno.impl.manager;

import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.modules.client.CustomFont;
import cope.inferno.Inferno;
import cope.inferno.impl.ui.font.CFontRenderer;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class FontManager implements Wrapper {
    private final FontRenderer defaultFontRenderer;
    private CFontRenderer customFontRender;

    private final String[] customFonts = new String[] { "comfortaa", "jetbrains_mono", "product_sans" };

    public FontManager() {
        Inferno.LOGGER.info("Adding custom fonts to Graphics Environment....");
        for (String fontName : this.customFonts) {
            Inferno.LOGGER.info("Loading custom font {}", fontName);

            InputStream stream = FontManager.class.getResourceAsStream("/assets/inferno/fonts/" + fontName + ".ttf");
            if (stream == null) {
                Inferno.LOGGER.error("Could not parse font {}.tff from resources", fontName);
                continue;
            }

            try {
                Font font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(Font.PLAIN, CustomFont.size.getValue());
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                stream.close();
            } catch (FontFormatException | IOException | NullPointerException e) {
                Inferno.LOGGER.error("Couldn't add custom font.\n{}", e.toString());
                continue;
            }

            Inferno.LOGGER.info("Added custom font {}.ttf to the Graphics Environment.", fontName);
        }

        this.defaultFontRenderer = mc.fontRenderer;
    }

    public void resetCustomFont() {
        this.customFontRender = new CFontRenderer(new Font(CustomFont.font.getValue(), CustomFont.style.getValue().getStyle(), CustomFont.size.getValue()), CustomFont.antiAlias.getValue(), CustomFont.fractionalMetrics.getValue());
    }

    public void drawString(String text, double x, double y, int color) {
        if (CustomFont.INSTANCE.isOn()) {
            this.customFontRender.drawString(text, (int) x, (int) y, color);
        } else {
            this.defaultFontRenderer.drawString(text, (int) x, (int) y, color);
        }
    }

    public void drawStringWithShadow(String text, double x, double y, int color) {
        if (CustomFont.INSTANCE.isOn()) {
            this.customFontRender.drawStringWithShadow(text, x, y, color);
        } else {
            this.defaultFontRenderer.drawStringWithShadow(text, (float) x, (float) y, color);
        }
    }

    public void drawCorrectString(String text, double x, double y, int color) {
        if (CustomFont.INSTANCE.isOn()) {
            if (CustomFont.shadow.getValue()) {
                this.drawStringWithShadow(text, x, y, color);
            } else {
                this.drawString(text, x, y, color);
            }
        } else {
            this.drawString(text, x, y, color);
        }
    }

    public int getHeight() {
        return CustomFont.INSTANCE.isOn() ? this.customFontRender.getHeight() : this.defaultFontRenderer.FONT_HEIGHT;
    }

    public int getWidth(String text) {
        return CustomFont.INSTANCE.isOn() ? this.customFontRender.getStringWidth(text) : this.defaultFontRenderer.getStringWidth(text);
    }
}
