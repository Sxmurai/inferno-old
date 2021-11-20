package me.sxmurai.inferno.impl.manager;

import com.google.common.collect.Lists;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.features.module.modules.client.CustomFont;
import me.sxmurai.inferno.impl.features.module.modules.client.GUI;
import me.sxmurai.inferno.impl.features.module.modules.client.Notifier;
import me.sxmurai.inferno.impl.features.module.modules.combat.*;
import me.sxmurai.inferno.impl.features.module.modules.miscellaneous.*;
import me.sxmurai.inferno.impl.features.module.modules.movement.*;
import me.sxmurai.inferno.impl.features.module.modules.player.*;
import me.sxmurai.inferno.impl.features.module.modules.visual.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ModuleManager {
    private final List<Module> modules;

    public ModuleManager() {
        this.modules = Lists.newArrayList(
                // client
                new CustomFont(),
                new GUI(),
                new Notifier(),

                // combat
                new Aura(),
                new AutoArmor(),
                new AutoBowRelease(),
                new AutoCrystal(),
                new AutoObsidian(),
                new Critcals(),
                new HoleFiller(),
                new Offhand(),
                new Quiver(),
                new SelfFill(),

                // miscellaneous
                new FakePlayer(),
                new NoHandshake(),
                new NoRotate(),
                new Suffix(),
                new TickShift(),

                // movement
                new FastFall(),
                new NoSlow(),
                new Speed(),
                new Sprint(),
                new Velocity(),

                // player
                new AntiVoid(),
                new AutoRespawn(),
                new FastUse(),
                new HotbarRefill(),
                new MiddleClick(),
                new MultiTask(),
                new Reach(),
                new Scaffold(),
                new Speedmine(),
                new Timer(),

                // visual
                new Brightness(),
                new Chams(),
                new Nametags(),
                new NoRender(),
                new PopChams(),
                new ViewClip(),
                new Wallhack()
        );

        this.modules.forEach(Module::registerAllSettings);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        int code = Keyboard.getEventKey();
        if (code != Keyboard.KEY_NONE && !Keyboard.getEventKeyState()) { // if the key is not unknown and the button is not down
            this.modules.forEach((module) -> {
                if (module.getBind() == code) {
                    module.toggle();
                }
            });
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
