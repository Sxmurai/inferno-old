package me.sxmurai.inferno.features.modules.movement;

import me.sxmurai.inferno.events.mc.UpdateEvent;
import me.sxmurai.inferno.features.settings.Setting;
import me.sxmurai.inferno.managers.modules.Module;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "Sprint", description = "Makes you automatically sprint", category = Module.Category.MOVEMENT)
public class Sprint extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.LEGIT);
    public final Setting<Boolean> hungerCheck = new Setting<>("HungerCheck", false);
    public final Setting<Boolean> strict = new Setting<>("Strict", false);

    @SubscribeEvent
    public void onUpdate(UpdateEvent event) {
        if (mode.getValue() == Mode.LEGIT && mc.gameSettings.keyBindForward.pressed || mode.getValue() == Mode.RAGE) {
            if ((hungerCheck.getValue() && mc.player.getFoodStats().getFoodLevel() <= 6) || (strict.getValue() && (mc.player.isSneaking() || mc.player.isHandActive() || mc.player.collidedHorizontally))) {
                mc.player.setSprinting(false);
                return;
            }

            mc.player.setSprinting(true);
        }
    }

    public enum Mode {
        LEGIT, RAGE
    }
}
