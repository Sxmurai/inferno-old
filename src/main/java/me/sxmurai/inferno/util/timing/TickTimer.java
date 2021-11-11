package me.sxmurai.inferno.util.timing;

public class TickTimer {
    // precision shit
    private final Timer timer = new Timer();

    public boolean passed(float ticks) {
        return this.timer.passedMs(50L * (long) ticks);
    }

    public boolean passed(int ticks) {
        return this.timer.passedMs(50L * ticks);
    }

    public TickTimer reset() {
        this.timer.reset();
        return this;
    }
}
