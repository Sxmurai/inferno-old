package me.sxmurai.inferno.impl.features.module.modules.visual;

import me.sxmurai.inferno.util.render.ColorUtil;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;

@Module.Define(name = "Chams", category = Module.Category.Visual)
@Module.Info(description = "Renders entities differently")
public class Chams extends Module {
    public static Chams INSTANCE;

    public final Option<Mode> mode = new Option<>("Mode", Mode.Normal);
    public final Option<ColorUtil.Color> color = new Option<>("Color", new ColorUtil.Color(255, 255, 255, 80), () -> this.mode.getValue() == Mode.Colored || this.mode.getValue() == Mode.XQZ);
    public final Option<ColorUtil.Color> hidden = new Option<>("Hidden", new ColorUtil.Color(210, 208, 214, 80), () -> this.mode.getValue() == Mode.XQZ);

    public Chams() {
        INSTANCE = this;
    }

    public enum Mode {
        Normal, Colored, XQZ
    }
}
