package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;

@Module.Define(name = "Timer", category = Module.Category.Player)
@Module.Info(description = "Changes the game's tick rate")
public class Timer extends Module {
    public final Setting<Boolean> sync = new Setting<>("Sync", false);
    public final Setting<Float> speed = new Setting<>("Speed", 1.0f, 0.1f, 20.0f, () -> !this.sync.getValue());

    @Override
    public String getDisplayInfo() {
        return String.valueOf(this.speed.getValue());
    }

    @Override
    protected void onDeactivated() {
        if (fullNullCheck()) {
            Inferno.tickManager.reset();
        }
    }

    @Override
    public void onUpdate() {
        if (this.sync.getValue()) {
            Inferno.tickManager.setTicks(1000.0f, (float) Inferno.serverManager.getTps());
        } else {
            Inferno.tickManager.setTicks(this.speed.getValue());
        }
    }
}
