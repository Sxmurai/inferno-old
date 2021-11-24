package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;

@Module.Define(name = "Interact", category = Module.Category.Player)
@Module.Info(description = "Changes how you interact with things")
public class Interact extends Module {
    public static Interact INSTANCE;

    public static final Setting<Boolean> noEntityTrace = new Setting<>("NoEntityTrace", false);
    public static final Setting<Boolean> liquidPlace = new Setting<>("LiquidPlace", false);

    public Interact() {
        INSTANCE = this;
    }
}
