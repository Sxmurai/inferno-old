package cope.inferno.core.manager.managers;

import cope.inferno.util.internal.Wrapper;

public class TickManager implements Wrapper {
    private float tickLength = 50.0f;

    public void onTick() {
        mc.timer.tickLength = tickLength;
    }

    public void setTickLength(float in) {
        tickLength = in;
    }

    public void setTicks(float factor, float in) {
        tickLength = factor / in;
    }

    public void reset() {
        tickLength = 50.0f;
    }
}
