package cope.inferno.impl.newui.components.windowed;

import cope.inferno.impl.newui.components.frames.AbstractFrame;
import net.minecraft.util.ResourceLocation;

public abstract class AbstractWindow extends AbstractFrame {
    protected static final ResourceLocation ICON_BACK_BUTTON = new ResourceLocation("/assets/inferno/textures/icon_back_button.png");
    protected static final ResourceLocation ICON_EXIT_BUTTON = new ResourceLocation("/assets/inferno/textures/icon_exit.png");

    public AbstractWindow(String id, double x, double y, double width, double height) {
        super(id, x, y, width, height);
    }

    // @todo
}
