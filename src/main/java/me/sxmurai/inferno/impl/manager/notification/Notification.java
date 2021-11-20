package me.sxmurai.inferno.impl.manager.notification;

import me.sxmurai.inferno.util.timing.Timer;

public class Notification {
    private final int id;
    private String text;

    private long delay;
    private final Timer timer = new Timer();

    public Notification(int id, String text) {
        this(id, text, -1L);
    }

    public Notification(int id, String text, long delay) {
        this.id = id;
        this.text = text;
        this.delay = delay;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getDelay() {
        return delay;
    }

    public int getId() {
        return id;
    }

    public boolean shouldDelete() {
        if (this.delay == -1L) {
            return false;
        }

        return this.timer.passedMs(this.delay);
    }
}
