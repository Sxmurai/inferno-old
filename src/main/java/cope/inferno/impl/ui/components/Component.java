package cope.inferno.impl.ui.components;

import cope.inferno.impl.features.Wrapper;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

public class Component implements Wrapper {
    protected final String name;
    protected double x = 0.0, y = 0.0;
    protected double width, height;

    public Component(String name) {
        this.name = name;
    }

    public void render(int mouseX, int mouseY) { }
    public void mouseClicked(int mouseX, int mouseY, int button) { }
    public void mouseReleased(int mouseX, int mouseY, int state) { }
    public void keyTyped(char character, int code) { }
    public void update() { }

    public String getName() {
        return name;
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

    public void playClickSound(float pitch) {
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, pitch));
    }

    public boolean isMouseInBounds(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isVisible() {
        return true;
    }
}
