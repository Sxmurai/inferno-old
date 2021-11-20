package me.sxmurai.inferno.impl.features.module.modules.render;

import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.render.ColorUtil;
import me.sxmurai.inferno.impl.features.module.Module;

@Module.Define(name = "Chams", category = Module.Category.Render)
@Module.Info(description = "Renders entities differently")
public class Chams extends Module {
    public static Chams INSTANCE;

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Normal);
    public final Setting<ColorUtil.Color> color = new Setting<>("Color", new ColorUtil.Color(255, 255, 255, 80), () -> this.mode.getValue() == Mode.Colored || this.mode.getValue() == Mode.XQZ);
    public final Setting<ColorUtil.Color> hidden = new Setting<>("Hidden", new ColorUtil.Color(210, 208, 214, 80), () -> this.mode.getValue() == Mode.XQZ);

    public Chams() {
        INSTANCE = this;
    }

    public enum Mode {
        Normal, Colored, XQZ
    }
}
