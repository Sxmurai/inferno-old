package cope.inferno.impl.manager;

import com.google.common.collect.Lists;
import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.features.module.modules.client.*;
import cope.inferno.impl.features.module.modules.combat.*;
import cope.inferno.impl.features.module.modules.miscellaneous.*;
import cope.inferno.impl.features.module.modules.movement.*;
import cope.inferno.impl.features.module.modules.player.*;
import cope.inferno.impl.features.module.modules.render.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ModuleManager implements Wrapper {
    private final List<Module> modules;

    public ModuleManager() {
        this.modules = Lists.newArrayList(
                // client
                new Colors(),
                new CustomFont(),
                new GUI(),
                new HUD(),
                new Notifier(),

                // combat
                new Aura(),
                new AutoArmor(),
                new AutoBowRelease(),
                new AutoCrystal(),
                new AutoGG(),
                new AutoLog(),
                new AutoTotem(),
                new Critcals(),
                new FastProjectile(),
                new HoleFiller(),
                new Quiver(),
                new SelfFill(),
                new Surround(),

                // miscellaneous
                new AutoRespawn(),
                new Disabler(),
                new FakePlayer(),
                new MiddleClick(),
                new NoHandshake(),
                new NoSoundLag(),
                new Suffix(),

                // movement
                new Anchor(),
                new ElytraFly(),
                new FastFall(),
                new Jesus(),
                new NoSlow(),
                new PacketFly(),
                new Speed(),
                new Sprint(),
                new Velocity(),

                // player
                new AntiVoid(),
                new FastUse(),
                new HotbarRefill(),
                new Interact(),
                new MultiTask(),
                new PingSpoof(),
                new Portal(),
                new Reach(),
                new Scaffold(),
                new Speedmine(),
                new Timer(),

                // render
                new Aspect(),
                new Brightness(),
                new Chams(),
                new ESP(),
                new Nametags(),
                new NoRender(),
                new ViewClip(),
                new Wallhack()
        );

        this.modules.forEach(Module::registerAllSettings);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        int code = Keyboard.getEventKey();
        if (mc.currentScreen == null && code != Keyboard.KEY_NONE && !Keyboard.getEventKeyState()) {
            for (Module module : this.modules) {
                if (module.getBind() == code) {
                    module.toggle();
                }
            }
        }
    }

    public <T extends Module> T getModule(String name) {
        for (Module module : this.modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return (T) module;
            }
        }

        return null;
    }

    public <T extends Module> T getModule(Class<? extends Module> clazz) {
        for (Module module : this.modules) {
            if (clazz.isInstance(module)) {
                return (T) module;
            }
        }

        return null;
    }

    public List<Module> getModules() {
        return modules;
    }
}
