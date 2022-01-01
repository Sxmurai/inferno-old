package cope.inferno.core.features.module.other;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;

public class Chat extends Module {
    public static Chat INSTANCE;

    public Chat() {
        super("Chat", Category.OTHER, "Modifies the chat");
        INSTANCE = this;
    }

    public static final Setting<Boolean> infinite = new Setting<>("Infinite", false);
    public static final Setting<Boolean> clear = new Setting<>("Clear", true);
}
