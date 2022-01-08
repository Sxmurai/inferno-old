package cope.inferno.core.manager.managers;

import cope.inferno.core.features.module.client.CustomFont;
import cope.inferno.core.gui.font.CustomFontRenderer;
import cope.inferno.util.internal.Wrapper;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;

public class FontManager implements Wrapper {
    private final FontRenderer defaultRenderer = mc.fontRenderer;
    private FontRenderer currentRenderer;

    public FontManager() {
        resetToDefault();
    }

    public void resetToDefault() {
        currentRenderer = defaultRenderer;
    }

    public void setFontRenderer(String name, int style, int size) {
        currentRenderer = new CustomFontRenderer(new Font(name, style, size));
    }

    public int drawNormalizedString(String text, double x, double y, int color) {
        if (CustomFont.INSTANCE.isToggled()) {
            if (CustomFont.shadow.getValue()) {
                return drawStringWithShadow(text, (float) x, (float) y, color);
            } else {
                return drawString(text, (float) x, (float) y, color);
            }
        } else {
            return drawString(text, (float) x, (float) y, color);
        }
    }

    public int drawString(String text, float x, float y, int color) {
        return currentRenderer.drawString(text, (int) x, (int) y, color);
    }

    public int drawStringWithShadow(String text, float x, float y, int color) {
        return currentRenderer.drawStringWithShadow(text, x, y, color);
    }

    public int getHeight() {
        return currentRenderer.FONT_HEIGHT;
    }

    public int getWidth(String text) {
        return currentRenderer.getStringWidth(text);
    }

    public Font[] getAvailableFonts() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    }

    public FontRenderer getCurrentRenderer() {
        return currentRenderer;
    }
}
