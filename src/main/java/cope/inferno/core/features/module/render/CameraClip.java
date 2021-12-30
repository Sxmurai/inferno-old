package cope.inferno.core.features.module.render;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;

public class CameraClip extends Module {
    public static CameraClip INSTANCE;

    public CameraClip() {
        super("CameraClip", Category.RENDER, "Clips your camera through blocks");
        INSTANCE = this;
    }

    public static final Setting<Double> distance = new Setting<>("Distance", 4.0, 1.0, 6.0);
}
