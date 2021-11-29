package cope.inferno.impl.newui.components.interactables;

import cope.inferno.impl.newui.components.AbstractComponent;
import org.lwjgl.input.Mouse;

public abstract class Slider extends AbstractComponent {
    private final float min;
    private final float difference;

    public Slider(String id, double x, double y, double width, double height, float min, float max) {
        super(id, x, y, width, height);
        this.min = min;
        this.difference = max - min;
    }

    @Override
    public void onRender(int mouseX, int mouseY) {
        this.doShit(mouseX, mouseY);
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button) {
        this.doShit(mouseX, mouseY);
    }

    public abstract void setValue(float percent);

    private void doShit(int mouseX, int mouseY) {
        if (this.canSetValue(mouseX, mouseY)) {
            this.setValue((float) (((double) (mouseX) - this.x) / (float) this.width));
        }
    }

    protected boolean canSetValue(int mouseX, int mouseY) {
        return this.isMouseInBounds(mouseX, mouseY) && Mouse.isButtonDown(0);
    }

    protected float part(float value) {
        return value - this.min;
    }

    protected float partialMultiplier(float value) {
        return this.part(value) / this.difference;
    }
}
