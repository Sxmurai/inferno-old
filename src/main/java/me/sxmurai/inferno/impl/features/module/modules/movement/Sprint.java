package me.sxmurai.inferno.impl.features.module.modules.movement;

import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.EnumConverter;
import me.sxmurai.inferno.impl.settings.Setting;
import net.minecraft.client.settings.KeyBinding;

@Module.Define(name = "Sprint", category = Module.Category.Movement)
@Module.Info(description = "Makes you automatically sprint")
public class Sprint extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Legit);

    @Override
    public String getDisplayInfo() {
        return EnumConverter.getActualName(this.mode.getValue());
    }

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
