package cope.inferno.util.render;

public class ColorUtil {
    public static float[] getColor(int color) {
        float alpha = (color >> 24 & 0xff) / 255f;
        float red = (color >> 16 & 0xff) / 255f;
        float green = (color >> 8 & 0xff) / 255f;
        float blue = (color & 0xff) / 255f;

        return new float[] { red, green, blue, alpha };
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        return (red << 16) + (green << 8) + blue + (alpha << 24);
    }
}
