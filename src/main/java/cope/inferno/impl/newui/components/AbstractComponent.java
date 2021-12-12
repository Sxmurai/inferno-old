package cope.inferno.impl.newui.components;

import cope.inferno.impl.features.Wrapper;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.util.ArrayList;

public abstract class AbstractComponent implements Wrapper {
    protected String id;
    protected double x, y;
    protected double width, height;

    protected final ArrayList<AbstractComponent> children = new ArrayList<>();

    public AbstractComponent(String id, double x, double y, double width, double height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void init();
    public abstract void onRender(int mouseX, int mouseY);

    public void onUpdate() { }
    public void onMouseClicked(int mouseX, int mouseY, int button) { }
    public void onMouseReleased(int mouseX, int mouseY, int state) { }
    public void onKeyTyped(char charTyped, int code) { }

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public boolean isMouseInBounds(int mouseX, int mouseY) {
        return isMouseWithinBounds(mouseX, mouseY, this.x, this.y, this.width, this.height);
    }

    public boolean isMouseWithinBounds(int mouseX, int mouseY, double x, double y, double w, double h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    public void playClickSound() {
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    public boolean isVisible() {
        return true;
    }

    public ArrayList<AbstractComponent> getChildren() {
        return children;
    }
}
