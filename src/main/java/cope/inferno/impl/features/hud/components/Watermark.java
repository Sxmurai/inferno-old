package cope.inferno.impl.features.hud.components;

import cope.inferno.impl.features.module.modules.client.Colors;
import cope.inferno.Inferno;
import cope.inferno.impl.features.hud.HudComponent;
import cope.inferno.impl.settings.Setting;

public class Watermark extends HudComponent {
    public final Setting<Boolean> version = new Setting<>("Version", true);

    public Watermark() {
        super("Watermark", "Shows the client's name and version");
    }

    @Override
    public void render() {
        String text = Inferno.NAME + (this.version.getValue() ? (" v" + Inferno.VERSION) : "");
        Inferno.fontManager.drawString(text, this.x, this.y, Colors.color());
    }
}
