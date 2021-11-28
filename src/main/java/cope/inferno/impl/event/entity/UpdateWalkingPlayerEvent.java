package cope.inferno.impl.event.entity;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class UpdateWalkingPlayerEvent extends Event {
    private final Era era;
    private float yaw, pitch;

    public UpdateWalkingPlayerEvent(Era era) {
        this(era, -1.0f, -1.0f);
    }

    public UpdateWalkingPlayerEvent(Era era, float yaw, float pitch) {
        this.era = era;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Era getEra() {
        return era;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public enum Era {
        PRE, POST
    }
}
