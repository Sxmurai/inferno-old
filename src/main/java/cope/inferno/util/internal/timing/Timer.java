package cope.inferno.util.internal.timing;

public class Timer {
    private long time = -1L;

    /**
     * Resets the timer back to the current time in nanoseconds
     */
    public void reset() {
        time = System.nanoTime();
    }

    /**
     * Checks if time has passed
     * @param in The time in the unit dependent on the format parameter
     * @param format The unit to use to convert into milliseconds
     * @return true if the time has passed, false if it has not
     */
    public boolean passed(long in, Format format) {
        return passedMs(format.convert(in));
    }

    /**
     * Checks if time in milliseconds has passed
     * @param in The time in milliseconds
     * @return true if the time has passed, false if it has not
     */
    public boolean passedMs(long in) {
        return System.nanoTime() - time >= in * 1000000L;
    }
}
