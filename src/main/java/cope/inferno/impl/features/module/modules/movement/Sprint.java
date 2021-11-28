package cope.inferno.impl.features.module.modules.movement;

import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;
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
            if (Wrapper.mc.player.getFoodStats().getFoodLevel() <= 6 || Wrapper.mc.player.isHandActive() || Wrapper.mc.player.isSneaking() || Wrapper.mc.player.collidedHorizontally) {
                return;
            }
        }

        KeyBinding.setKeyBindState(Wrapper.mc.gameSettings.keyBindSprint.getKeyCode(), true);
    }

    public enum Mode {
        Legit, Rage
    }
}
