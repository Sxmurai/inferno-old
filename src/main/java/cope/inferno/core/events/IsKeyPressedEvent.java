package cope.inferno.core.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class IsKeyPressedEvent extends Event {
    private final int keyCode;
    private boolean pressed;

    public IsKeyPressedEvent(int keyCode, boolean pressed) {
        this.keyCode = keyCode;
        this.pressed = pressed;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }
}
