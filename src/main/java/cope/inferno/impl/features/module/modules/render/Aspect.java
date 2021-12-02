package cope.inferno.impl.features.module.modules.render;

import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;

@Module.Define(name = "Aspect", category = Module.Category.Render)
@Module.Info(description = "Changes the aspect ratio")
public class Aspect extends Module {
    public static Aspect INSTANCE;

    public static final Setting<Float> ratio = new Setting<>("Ratio", (float) (mc.displayWidth / mc.displayHeight), 0.0f, 3.0f);

    public Aspect() {
        INSTANCE = this;
    }
}
