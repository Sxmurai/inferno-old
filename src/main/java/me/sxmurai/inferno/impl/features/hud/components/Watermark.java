package me.sxmurai.inferno.impl.features.hud.components;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.hud.HudComponent;
import me.sxmurai.inferno.impl.settings.Setting;

public class Watermark extends HudComponent {
    public final Setting<Boolean> version = new Setting<>("Version", true);

    public Watermark() {
        super("Watermark");
    }

    @Override
    public void render() {
        String text = Inferno.NAME + (this.version.getValue() ? (" v" + Inferno.VERSION) : "");
        Inferno.fontManager.drawString(text, this.x, this.y, -1);
    }
}
