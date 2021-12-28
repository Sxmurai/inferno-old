package cope.inferno.core.features.module.player;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;

public class Timer extends Module {
    public Timer() {
        super("Timer", Category.PLAYER, "Modifies your game's tick speed");
    }

    public static final Setting<Float> speed = new Setting<>("Speed", 1.5f, 0.1f, 20.0f);

    @Override
    protected void onDisable() {
        getInferno().getTickManager().reset();
    }

    @Override
    public void onUpdate() {
        getInferno().getTickManager().setTicks(50.0f, speed.getValue());
    }
}
