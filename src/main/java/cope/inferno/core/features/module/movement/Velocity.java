package cope.inferno.core.features.module.movement;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;

public class Velocity extends Module {
    public Velocity() {
        super("Velocity", Category.MOVEMENT, "Prevents vanilla velocity modifications");
    }

    // modifications
    public static final Setting<Float> horizontal = new Setting<>("Horizontal", 0.0f, 0.0f, 100.0f);
    public static final Setting<Float> vertical = new Setting<>("Vertical", 0.0f, 0.0f, 100.0f);

    // other shit
    public static final Setting<Boolean> ice = new Setting<>("Ice", false);
    public static final Setting<Boolean> liquids = new Setting<>("Liquids", false);
    public static final Setting<Boolean> blocks = new Setting<>("Blocks", true);
    public static final Setting<Boolean> push = new Setting<>("Push", true);
}
