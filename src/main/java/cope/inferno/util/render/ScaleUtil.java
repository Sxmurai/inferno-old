package cope.inferno.util.render;

import cope.inferno.core.Inferno;
import cope.inferno.util.internal.Wrapper;

public class ScaleUtil implements Wrapper {
    public static double alignV(String text, double x, double width) {
        return x + (width / 2.0) - (Inferno.INSTANCE.getFontManager().getWidth(text) / 2.0);
    }

    public static double alignH(double y, double height) {
        return (y + (height / 2.0)) - Inferno.INSTANCE.getFontManager().getHeight() / 2.0;
    }
}
