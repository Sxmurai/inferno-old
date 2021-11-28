package me.sxmurai.inferno.util.render;

import java.awt.*;

public class RainbowUtil {
    public static int color(float hue, float saturation, float brightness) {
        return Color.getHSBColor(hue, saturation, brightness).getRGB();
    }

    public static int rainbow(double delay, float saturation, float brightness) {
        double state = Math.ceil((System.currentTimeMillis() + delay) / 20.0) % 360;
        return color((float) (state / 360.f), saturation, brightness);
    }
}
