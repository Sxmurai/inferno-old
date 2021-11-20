package me.sxmurai.inferno.impl.event.entity;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class UpdateWalkingPlayerEvent extends Event {
    private final Era era;

    public UpdateWalkingPlayerEvent(Era era) {
        this.era = era;
    }

    public Era getEra() {
        return era;
    }

    public enum Era {
        PRE, POST
    }
}
