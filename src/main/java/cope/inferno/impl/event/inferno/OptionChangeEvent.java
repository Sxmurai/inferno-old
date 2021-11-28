package cope.inferno.impl.event.inferno;

import cope.inferno.impl.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.Event;

public class OptionChangeEvent extends Event {
    private final Setting setting;

    public OptionChangeEvent(Setting setting) {
        this.setting = setting;
    }

    public Setting getOption() {
        return setting;
    }
}
