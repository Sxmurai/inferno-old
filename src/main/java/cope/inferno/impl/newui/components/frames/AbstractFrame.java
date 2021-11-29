package cope.inferno.impl.newui.components.frames;

import cope.inferno.impl.newui.components.AbstractComponent;
import org.lwjgl.input.Mouse;

import java.awt.*;

public abstract class AbstractFrame extends AbstractComponent {
    protected static final int BACKGROUND_COLOR = new Color(35, 39, 42).getRGB();
    protected static final double DEFAULT_HEIGHT = 235.0;
    protected static final double BAR_HEIGHT = 16.0;

    // @todo, resizing too?
    private boolean dragging = false;
    protected double x2, y2;

    public AbstractFrame(String id, double x, double y, double width, double height) {
        super(id, x, y, width, height);
    }

    // state - true = up
    // state - false = down
    public abstract void onScroll(int offset, boolean state);

    @Override
    public void onRender(int mouseX, int mouseY) {
        if (this.dragging) {
            this.x = this.x2 - mouseX;
            this.y = this.y2 - mouseY;
        }

        if (this.isMouseInBounds(mouseX, mouseY)) {
            int scroll = Mouse.getDWheel();
            this.onScroll(scroll, scroll < 0);
        }
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0 && this.isMouseWithinBounds(mouseX, mouseY, this.x, this.y, this.width, BAR_HEIGHT)) {
            this.x2 = this.x - mouseX;
            this.y2 = this.y - mouseY;
            this.dragging = true;
        }
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) {
            this.dragging = false;
        }
    }
}
