package me.sxmurai.inferno.impl.features.module.modules.render;

import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;

@Module.Define(name = "ViewClip", category = Module.Category.Render)
@Module.Info(description = "Clips your third person camera through blocks")
public class ViewClip extends Module {
    public static ViewClip INSTANCE;

    public static final Setting<Double> distance = new Setting<>("Distance", 4.5, 1.0, 10.0);

    public ViewClip() {
        INSTANCE = this;
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(distance.getValue());
    }
}
