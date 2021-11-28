package cope.inferno.util.render;

public class ColorUtil {
    public static Color getColor(int color) {
        float alpha = (color >> 24 & 0xff) / 255f;
        float red = (color >> 16 & 0xff) / 255f;
        float green = (color >> 8 & 0xff) / 255f;
        float blue = (color & 0xff) / 255f;

        return new Color(red, green, blue, alpha);
    }

    public static Color getActualColors(Color glColor) {
        return new Color(glColor.red * 255.0f, glColor.green * 255.0f, glColor.blue * 255.0f, glColor.alpha * 255.0f);
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        return (red << 16) + (green << 8) + blue + (alpha << 24);
    }

    public static class Color {
        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;

        public Color(float red, float green, float blue, float alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        public float getRed() {
            return red;
        }

        public float getGreen() {
            return green;
        }

        public float getBlue() {
            return blue;
        }

        public float getAlpha() {
            return alpha;
        }
    }
}
