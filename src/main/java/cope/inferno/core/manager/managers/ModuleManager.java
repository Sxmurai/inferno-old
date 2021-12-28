package cope.inferno.core.manager.managers;

import cope.inferno.core.features.module.Module;
import cope.inferno.core.features.module.client.ClickGUI;
import cope.inferno.core.features.module.client.Notifier;
import cope.inferno.core.features.module.combat.Criticals;
import cope.inferno.core.features.module.movement.Sprint;
import cope.inferno.core.features.module.player.Timer;
import cope.inferno.core.manager.Manager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class ModuleManager extends Manager<Module> {
    @Override
    public void init() {
        // client
        modules.add(new ClickGUI());
        modules.add(new Notifier());

        // combat
        modules.add(new Criticals());

        // movement
        modules.add(new Sprint());

        // player
        modules.add(new Timer());

        LOGGER.info("Loaded {} modules.", modules.size());

        modules.forEach(Module::register);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        int code = Keyboard.getEventKey();
        if (mc.currentScreen == null && code != Keyboard.KEY_NONE && !Keyboard.getEventKeyState()) {
            for (Module module : modules) {
                if (module.getBind() == code) {
                    module.toggle();
                }
            }
        }
    }
}
