package cope.inferno.core.features.module.other;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;

// im trying bro
public class FullScreenshot extends Module {
    public static FullScreenshot INSTANCE;

    public FullScreenshot() {
        super("FullScreenshot", Category.OTHER, "Takes a screenshot of the entire screen rather than just the game window");
        INSTANCE = this;
    }
}
