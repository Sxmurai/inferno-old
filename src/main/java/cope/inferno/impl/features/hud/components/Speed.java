package cope.inferno.impl.features.hud.components;

import cope.inferno.impl.features.module.modules.client.Colors;
import cope.inferno.Inferno;
import cope.inferno.impl.features.hud.HudComponent;
import cope.inferno.impl.settings.Setting;

public class Speed extends HudComponent {
    public final Setting<Measurement> measurement = new Setting<>("Measurement", Measurement.KMH);

    public Speed() {
        super("Speed", "Shows your speed");
    }

    @Override
    public void render() {
        String display = this.getSpeedDisplay();
        Inferno.fontManager.drawCorrectString(display, this.x, this.y, Colors.color());

        this.width = Inferno.fontManager.getWidth(display);
        this.height = Inferno.fontManager.getHeight();
    }

    private String getSpeedDisplay() {
        double speed = this.toKmh(Inferno.serverManager.getSpeed());
        if (this.measurement.getValue() == Measurement.MPS) {
            speed /= 3.6;
        }

        return (Math.round(10.0 * speed) / 10.0) + " " + this.measurement.getValue().display;
    }

    private double toKmh(double speed) {
        return Math.sqrt(speed) * 71.2729367892;
    }

    public enum Measurement {
        KMH("km/h"), MPS("m/s");

        private final String display;
        Measurement(String display) {
            this.display = display;
        }
    }
}
