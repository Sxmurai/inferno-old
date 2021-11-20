package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;

@Module.Define(name = "Reach", category = Module.Category.Player)
public class Reach extends Module {
    public static Reach INSTANCE;

    public static final Setting<Float> range = new Setting<>("Range", 4.5f, 4.5f, 10.0f);

    public Reach() {
        INSTANCE = this;
    }
}
