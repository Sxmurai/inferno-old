package cope.inferno.core.gui.font;

import net.minecraft.client.renderer.texture.DynamicTexture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL11.*;

public class AWTFont {
    private final Font font;
    private DynamicTexture texture;

    private final Set<CharData> characters = new HashSet<>();

    public AWTFont(Font font) {
        this.font = font;

        // create our bitmap image
        this.createBitmap();
    }

    protected void drawChar(CharData charData, float x, float y) {
        float renderSRCX = charData.getX() / 512.0f;
        float renderSRCY = charData.getY() / 512.0f;
        float renderSRCWidth = charData.getWidth() / 512.0f;
        float renderSRCHeight = charData.getHeight() / 512.0f;

        glBegin(4);

        glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
        glVertex2d(x + charData.getWidth(), y);
        glTexCoord2f(renderSRCX, renderSRCY);
        glVertex2d(x, y);
        glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
        glVertex2d(x, y + charData.getHeight());
        glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
        glVertex2d(x, y + charData.getHeight());
        glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY + renderSRCHeight);
        glVertex2d(x + charData.getWidth(), y + charData.getHeight());
        glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
        glVertex2d(x + charData.getWidth(), y);

        glEnd();
    }

    private void createBitmap() {
        // create our bitmap image
        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        // get our graphics environment
        Graphics2D graphics = (Graphics2D) image.getGraphics();

        // configure the font we'll be using in this bitmap
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        // set our rendering hints for better looking fonts
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // get our font metrics object
        FontMetrics metrics = graphics.getFontMetrics();

        int x = 0;
        int y = metrics.getHeight();

        for (int glyph = 0; glyph < font.getNumGlyphs(); ++glyph) {
            char glyphCharacter = (char) glyph;

            // if we cannot display this glyph, don't bother to write to the bitmap
            if (!font.canDisplay(glyphCharacter)) {
                continue;
            }

            // get the width of this glyph
            int charWidth = metrics.charWidth(glyphCharacter) + metrics.getLeading() + 1;
            int charHeight = metrics.getHeight();

            // make sure our character will not exceed our bounds
            if (x + charHeight >= 512) {
                x = 0;
                y += charHeight + 1;
            }

            // add our character data
            CharData data = new CharData(glyphCharacter, x, y, charWidth, charHeight);
            characters.add(data);

            // draw glyph to the bitmap
            graphics.drawString(String.valueOf(data.getCharacter()), data.getX() + 1, y + metrics.getAscent());

            // add our current width to not overlap other characters in the bitmap
            x += charWidth;
        }

        // we now have our image, let's generate our DynamicTexture which we'll use for rendering
        // this will automatically upload the texture so we can just use it
        texture = new DynamicTexture(image);
    }

    public Set<CharData> getCharacters() {
        return characters;
    }

    public CharData getChar(char c) {
        return characters.stream().filter((charData) -> charData.getCharacter() == c).findFirst().orElse(null);
    }

    public DynamicTexture getTexture() {
        return texture;
    }
}
