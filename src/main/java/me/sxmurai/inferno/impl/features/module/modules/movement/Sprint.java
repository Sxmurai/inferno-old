package me.sxmurai.inferno.impl.features.module.modules.movement;

import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;
import net.minecraft.client.settings.KeyBinding;

@Module.Define(name = "Sprint", category = Module.Category.Movement)
@Module.Info(description = "Makes you automatically sprint")
public class Sprint extends Module {
    public final Option<Mode> mode = new Option<>("Mode", Mode.Legit);

    @Override
    public void onUpdate() {
        if (this.mode.getValue() == Mode.Legit) {
            if (mc.player.getFoodStats().getFoodLevel() <= 6 || mc.player.isHandActive() || mc.player.isSneaking() || mc.player.collidedHorizontally) {
                return;
            }
        }

        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
    }

    public enum Mode {
        Legit, Rage
    }
}
