package cope.inferno.core.features.module.movement;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;

public class AntiVoid extends Module {
    public AntiVoid() {
        super("AntiVoid", Category.MOVEMENT, "Stops you from falling into the void");
    }

    @Override
    public void onUpdate() {
        if (mc.player.posY <= 0.0) {
            mc.player.motionY = 0.0;
        }
    }
}
