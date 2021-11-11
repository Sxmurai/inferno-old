package me.sxmurai.inferno.util.render;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.util.Util;

public class ScaleUtil implements Util {
    public static float centerTextX(float x, float width, float textWidth) {
        return x + (width / 2.0f) - (textWidth / 2.0f);
    }

    public static float centerTextY(float y, float height) {
        return (y + (height / 2.0f)) - (Inferno.fontManager.getHeight() / 2.0f);
    }
}
