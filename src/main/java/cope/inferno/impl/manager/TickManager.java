package cope.inferno.impl.manager;

import cope.inferno.impl.features.Wrapper;

public class TickManager implements Wrapper {
    private float tickLength = 50.0f;

    public void onUpdate() {
        mc.timer.tickLength = this.tickLength;
    }

    public void setTickLength(float tickLength) {
        this.tickLength = tickLength;
    }

    public void setTicks(float ticks) {
        this.setTicks(50.0f, ticks);
    }

    public void setTicks(float factor, float ticks) {
        this.setTickLength(factor / ticks);
    }

    public void reset() {
        this.tickLength = 50.0f;
    }

    public float getTickLength() {
        return tickLength;
    }
}
