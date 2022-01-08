package cope.inferno.core.manager.managers;

import cope.inferno.core.features.module.Module;
import cope.inferno.core.features.module.client.ClickGUI;
import cope.inferno.core.features.module.client.CustomFont;
import cope.inferno.core.features.module.client.Notifier;
import cope.inferno.core.features.module.combat.*;
import cope.inferno.core.features.module.movement.*;
import cope.inferno.core.features.module.other.*;
import cope.inferno.core.features.module.player.*;
import cope.inferno.core.features.module.render.CameraClip;
import cope.inferno.core.features.module.render.ESP;
import cope.inferno.core.features.module.render.Fullbright;
import cope.inferno.core.features.module.render.Nametags;
import cope.inferno.core.manager.Manager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class ModuleManager extends Manager<Module> {
    @Override
    public void init() {
        // client
        modules.add(new ClickGUI());
        modules.add(new CustomFont());
        modules.add(new Notifier());

        // combat
        modules.add(new Aura());
        modules.add(new Auto32K());
        modules.add(new AutoClip());
        modules.add(new AutoTotem());
        modules.add(new BowRelease());
        modules.add(new Criticals());
        modules.add(new FeetTrap());
        // modules.add(new HoleFiller());
        modules.add(new SelfFill());

        // movement
        modules.add(new AntiVoid());
        // modules.add(new Jesus()); // @todo broken
        modules.add(new NoFall());
        modules.add(new NoSlow());
        modules.add(new PacketFly());
        modules.add(new Sprint());
        modules.add(new Velocity());

        // other
        modules.add(new AntiBookBan());
        modules.add(new Chat());
        modules.add(new FakePlayer());
        modules.add(new FullScreenshot());
        modules.add(new MiddleClick());
        modules.add(new Rubberband());
        modules.add(new XCarry());

        // player
        modules.add(new FastPlace());
        modules.add(new Interact());
        modules.add(new PingSpoof());
        modules.add(new Reach());
        modules.add(new Replenish()); // @todo broken???? https://pastebin.com/RrMuVSFJ
        modules.add(new Scaffold());
        modules.add(new Speedmine());
        modules.add(new Timer());

        // render
        modules.add(new CameraClip());
        modules.add(new ESP());
        modules.add(new Fullbright());
        modules.add(new Nametags());

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
