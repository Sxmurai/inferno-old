package cope.inferno.impl.newui.main.clickgui;

import cope.inferno.impl.newui.components.frames.DefaultFrame;

public abstract class ModuleFrame extends DefaultFrame {
    public ModuleFrame(String id, double x, double y) {
        super(id, x, y, 92.0, DEFAULT_HEIGHT);
        this.init();
    }
}
