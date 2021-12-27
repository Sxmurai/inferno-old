package cope.inferno.core.events;

import cope.inferno.core.features.module.Module;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ModuleToggleEvent extends Event {
    private final Module module;
    private final boolean state;

    public ModuleToggleEvent(Module module, boolean state) {
        this.module = module;
        this.state = state;
    }

    public Module getModule() {
        return module;
    }

    public boolean getState() {
        return state;
    }
}
