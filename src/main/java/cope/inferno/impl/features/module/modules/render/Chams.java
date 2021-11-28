package cope.inferno.impl.features.module.modules.render;

import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.render.ColorUtil;

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

    @Override
    public String getDisplayInfo() {
        return EnumConverter.getActualName(this.mode.getValue());
    }

    public enum Mode {
        Normal, Colored, XQZ
    }
}
