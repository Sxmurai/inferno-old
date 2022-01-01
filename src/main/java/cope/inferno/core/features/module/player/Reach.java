package cope.inferno.core.features.module.player;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;

public class Reach extends Module {
    public static Reach INSTANCE;

    public Reach() {
        super("Reach", Category.PLAYER, "Makes you reach further");
        INSTANCE = this;
    }

    public static final Setting<Float> distance = new Setting<>("Distance", 4.2f, 1.0f, 20.0f);
}
