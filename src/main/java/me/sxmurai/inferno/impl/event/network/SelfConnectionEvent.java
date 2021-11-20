package me.sxmurai.inferno.impl.event.network;

import net.minecraftforge.fml.common.eventhandler.Event;

public class SelfConnectionEvent extends Event {
    public final Type type;

    public SelfConnectionEvent(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        Connect, Disconnect
    }
}
