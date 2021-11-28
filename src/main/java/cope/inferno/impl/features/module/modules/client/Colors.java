package cope.inferno.impl.features.module.modules.client;

import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.render.RainbowUtil;

@Module.Define(name = "Color", category = Module.Category.Client)
@Module.Info(description = "Manages the colors for things on the client")
public class Colors extends Module {
    public static final Setting<Boolean> rainbow = new Setting<>("Rainbow", true);
    public static final Setting<Float> hue = new Setting<>("Hue", 360.0f, 0.0f, 360.0f);
    public static final Setting<Float> saturation = new Setting<>("Saturation", 1.0f, 0.0f, 1.0f);
    public static final Setting<Float> brightness = new Setting<>("Brightness", 1.0f, 0.0f, 1.0f);

    public Colors() {
        this.toggle();
    }

    @Override
    protected void onDeactivated() {
        this.toggle();
    }

    public static int color() {
        float s = saturation.getValue();
        float b = brightness.getValue();
        return rainbow.getValue() ? RainbowUtil.rainbow(1.0, s, b) : RainbowUtil.color(hue.getValue(), s, b);
    }
}
